#pragma once

namespace styxlib
{
    class ExpectedSizeResult
    {
    private:
        Size _value;
        ErrorCode _error;
    public:
        ExpectedSizeResult(Size value) : _value(value), _error(ErrorCode::Success) {}
        ExpectedSizeResult(ErrorCode error) : _value(0), _error(error) {}
        Size value() const { return _value; }
        ErrorCode error() const { return _error; }
        bool has_value() const { return _error == ErrorCode::Success; }
    };

    using SizeResult = ExpectedSizeResult;

    /**
     * Reads the first X bytes of buffer and interprets them as a packet size.
     * @param headerSize The size of the packet header (1, 2, or 4 bytes).
     * @param buffer The buffer to read from.
     * @param bufferSize The number of valid bytes available in the buffer.
     * @return The decoded packet payload size, or an ErrorCode if the buffer
     *         is too small to hold the header.
     */
    SizeResult getPacketSize(
        const PacketHeaderSize &headerSize,
        const uint8_t *buffer,
        Size bufferSize);
}