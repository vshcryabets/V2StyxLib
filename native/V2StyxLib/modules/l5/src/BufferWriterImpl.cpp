#include "serialization/BufferWriterImpl.h"
#include <stdexcept>

BufferWriterImpl::BufferWriterImpl(size_t bufferSize)
{
    buffer = new uint8_t[bufferSize];
    position = 0;
    limit = bufferSize;
}

BufferWriterImpl::~BufferWriterImpl()
{
    delete[] buffer;
}

void BufferWriterImpl::writeUInt8(uint8_t val)
{
    if (position + sizeof(uint8_t) > limit)
    {
        throw std::out_of_range("Buffer overflow");
    }
    buffer[position++] = val;
}
void BufferWriterImpl::writeUInt16(uint16_t val)
{
    if (position + sizeof(uint16_t) > limit)
    {
        throw std::out_of_range("Buffer overflow");
    }
    buffer[position++] = static_cast<uint8_t>(val & 0xFF);
    buffer[position++] = static_cast<uint8_t>(val >> 8);
}
void BufferWriterImpl::writeUInt32(uint32_t val)
{
    if (position + sizeof(uint32_t) > limit)
    {
        throw std::out_of_range("Buffer overflow");
    }
    buffer[position++] = static_cast<uint8_t>(val & 0xFF);
    buffer[position++] = static_cast<uint8_t>(val >> 8);
    buffer[position++] = static_cast<uint8_t>(val >> 16);
    buffer[position++] = static_cast<uint8_t>(val >> 24);
}
void BufferWriterImpl::writeUInt64(uint64_t value)
{
    if (position + sizeof(uint64_t) > limit)
    {
        throw std::out_of_range("Buffer overflow");
    }
    buffer[position++] = static_cast<uint8_t>(value & 0xFF);
    buffer[position++] = static_cast<uint8_t>(value >> 8);
    buffer[position++] = static_cast<uint8_t>(value >> 16);
    buffer[position++] = static_cast<uint8_t>(value >> 24);
    buffer[position++] = static_cast<uint8_t>(value >> 32);
    buffer[position++] = static_cast<uint8_t>(value >> 40);
    buffer[position++] = static_cast<uint8_t>(value >> 48);
    buffer[position++] = static_cast<uint8_t>(value >> 56);
}
void BufferWriterImpl::writeUTFString(StyxString string)
{
    size_t len = string.length();
    if (len > limit - position - sizeof(uint16_t))
    {
        throw std::out_of_range("Buffer overflow");
    }
    writeUInt16(static_cast<uint16_t>(len));
    for (size_t i = 0; i < len; ++i)
    {
        buffer[position++] = static_cast<uint8_t>(string[i]);
    }
}

StyxSize BufferWriterImpl::write(const StyxBuffer data, StyxSize count)
{
    if (position + count > limit)
    {
        throw std::out_of_range("Buffer overflow");
    }
    for (StyxSize i = 0; i < count; ++i)
    {
        buffer[position++] = data[i];
    }
    return count;
}
void BufferWriterImpl::prepareBuffer(StyxSize bufferSize)
{
    delete[] buffer;
    buffer = new uint8_t[bufferSize];
    position = 0;
    limit = bufferSize;
}

StyxBuffer BufferWriterImpl::getBuffer() const
{
    return buffer;
}