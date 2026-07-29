import serial
from .Channels import ChannelRx, ChannelTx, StyxBuffer

class ChannelDriverUart(ChannelRx, ChannelTx):
   
    def __init__(self, port: str, 
                 baudrate: int = 115200, 
                 packetSizeHeader: int = 2, 
                 ioUnit: int = 8192,
                 streamingMode: bool = False,
                 sof1 = 0xAA, 
                 sof2 = 0x55):
        self.serial = serial.Serial(port, baudrate)
        self.packetSizeHeader = packetSizeHeader
        self.ioUnit = ioUnit
        self.streamingMode = streamingMode
        self.sof1 = sof1
        self.sof2 = sof2
        self._pending = b''

    ## Debug method just send raw bytes without any framing, for testing purposes
    def debugSendBytes(self, data: bytes) -> None:
        """Helper to send raw bytes for testing."""
        self.serial.write(data)

    def send_buffer(self, buffer: StyxBuffer) -> None:
        payload = buffer.buffer[:buffer.size]
        if self.streamingMode:
            self.serial.write(bytes([self.sof1, self.sof2]) + payload) ## TODO Add CRC16
        else:
            self.serial.write(payload)

    def get_status(self) -> str:
        return "UART: OK"

    # ------------------------------------------------------------------ #
    #  Internal helpers                                                    #
    # ------------------------------------------------------------------ #

    def _drain_serial(self) -> None:
        """Move all currently available serial bytes into the pending buffer."""
        waiting = self.serial.in_waiting
        if waiting:
            self._pending += self.serial.read(waiting)

    def _read_exactly(self, n: int) -> bytes:
        """Read exactly n bytes, blocking until all are available.
        Consumes from _pending first, then falls back to the serial port."""
        buf = b''
        if self._pending:
            take = min(n, len(self._pending))
            buf = self._pending[:take]
            self._pending = self._pending[take:]
        while len(buf) < n:
            chunk = self.serial.read(n - len(buf))
            if chunk:
                buf += chunk
        return buf

    def _find_sof_nonblocking(self) -> bool:
        """Scan the pending buffer for the SOF marker pair without blocking.
        Bytes before (and including) the marker are discarded.
        Returns True if the marker was found and pending is positioned right after it.
        Keeps a potential partial match (trailing sof1) for the next call."""
        self._drain_serial()
        data = self._pending
        for i in range(len(data) - 1):
            if data[i] == self.sof1 and data[i + 1] == self.sof2:
                self._pending = data[i + 2:]
                return True
        # Preserve a trailing sof1 in case sof2 arrives next call
        self._pending = bytes([data[-1]]) if data and data[-1] == self.sof1 else b''
        return False

    def _find_sof_blocking(self) -> None:
        """Block until the SOF marker pair is found, consuming bytes up to and including it."""
        while True:
            b = self._read_exactly(1)[0]
            if b != self.sof1:
                continue
            b = self._read_exactly(1)[0]
            if b == self.sof2:
                return
            # If the second byte is itself sof1, push it back so the next
            # iteration treats it as a potential new marker start.
            if b == self.sof1:
                self._pending = bytes([b]) + self._pending

    def _read_packet(self) -> StyxBuffer:
        """Read header + payload (blocking). Shared by both public read methods."""
        header_bytes = self._read_exactly(self.packetSizeHeader)
        packet_size = int.from_bytes(header_bytes, byteorder='big')
        data = self._read_exactly(packet_size)
        return StyxBuffer(data, packet_size)

    # ------------------------------------------------------------------ #
    #  Public interface                                                    #
    # ------------------------------------------------------------------ #

    def readNonBlocking(self) -> StyxBuffer:
        """Return the next complete packet if one is fully available, otherwise StyxBuffer(b'', 0)."""
        if self.streamingMode:
            if not self._find_sof_nonblocking():
                return StyxBuffer(b'', 0)
            # Refresh pending after SOF was consumed, then check header is available
            self._drain_serial()
            if len(self._pending) < self.packetSizeHeader:
                return StyxBuffer(b'', 0)
        else:
            self._drain_serial()
            if len(self._pending) <= self.packetSizeHeader:
                return StyxBuffer(b'', 0)
        return self._read_packet()

    def readBlocking(self) -> StyxBuffer:
        """Block until a complete packet is received and return it."""
        if self.streamingMode:
            self._find_sof_blocking()
        return self._read_packet()