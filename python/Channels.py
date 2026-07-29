from abc import ABC, abstractmethod
from typing import Union

class StyxBuffer:
    def __init__(self, buffer: Union[bytes, bytearray], size: int):
        self.buffer = buffer
        self.size = size

class ChannelRx(ABC):
    @abstractmethod
    def readNonBlocking(self) -> StyxBuffer:
        pass

    @abstractmethod
    def readBlocking(self) -> StyxBuffer:
        pass

class ChannelTx(ABC):
    @abstractmethod
    def send_buffer(self, buffer: StyxBuffer) -> None:
        pass

class StyxLibCrc16:
    V2STYXLIB_CRC16_POLY = 0x1021  # CCIT polynomial
    V2STYXLIB_CRC16_INITIAL_VALUE = 0xFFFF

    @staticmethod
    def update(crc: int, data: int) -> int:
        crc ^= (data << 8) & 0xFFFF    
        for _ in range(8):
            if crc & 0x8000:
                crc = ((crc << 1) ^ StyxLibCrc16.V2STYXLIB_CRC16_POLY) & 0xFFFF
            else:
                crc = (crc << 1) & 0xFFFF
        return crc
    
    @staticmethod
    def calculate(data: bytes) -> int:
        crc = StyxLibCrc16.V2STYXLIB_CRC16_INITIAL_VALUE
        for byte in data:
            crc = StyxLibCrc16.update(crc, byte)
        return crc