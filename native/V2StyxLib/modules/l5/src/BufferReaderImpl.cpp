#include "serialization/BufferReaderImpl.h"
#include <stdexcept>

BufferReaderImpl::BufferReaderImpl(const StyxBuffer &buf, Styx::Size size)
    : buffer(buf), position(0), limit(size)
{
    if (buf == nullptr) {
        throw std::invalid_argument("Buffer cannot be null");
    }
}

BufferReaderImpl::~BufferReaderImpl()
{

}

uint8_t BufferReaderImpl::readUInt8()
{
    if (position + sizeof(uint8_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    return buffer[position++];
}

uint16_t BufferReaderImpl::readUInt16()
{
    if (position + sizeof(uint16_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    uint16_t value = static_cast<uint16_t>(buffer[position]) | 
                     (static_cast<uint16_t>(buffer[position + 1]) << 8);
    position += sizeof(uint16_t);
    return value;
}

uint32_t BufferReaderImpl::readUInt32()
{
    if (position + sizeof(uint32_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    uint32_t value = static_cast<uint32_t>(buffer[position]) |
                     (static_cast<uint32_t>(buffer[position + 1]) << 8) |
                     (static_cast<uint32_t>(buffer[position + 2]) << 16) |
                     (static_cast<uint32_t>(buffer[position + 3]) << 24);
    position += sizeof(uint32_t);
    return value;
}

uint64_t BufferReaderImpl::readUInt64()
{
    if (position + sizeof(uint64_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    uint64_t value = static_cast<uint64_t>(buffer[position]) |
                     (static_cast<uint64_t>(buffer[position + 1]) << 8) |
                     (static_cast<uint64_t>(buffer[position + 2]) << 16) |
                     (static_cast<uint64_t>(buffer[position + 3]) << 24) |
                     (static_cast<uint64_t>(buffer[position + 4]) << 32) |
                     (static_cast<uint64_t>(buffer[position + 5]) << 40) |
                     (static_cast<uint64_t>(buffer[position + 6]) << 48) |
                     (static_cast<uint64_t>(buffer[position + 7]) << 56);
    position += sizeof(uint64_t);
    return value;
}

StyxString BufferReaderImpl::readUTFString()
{
    if (position + sizeof(uint16_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    uint16_t length = readUInt16();
    if (position + length > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    StyxString result;
    result.reserve(length);
    for (uint16_t i = 0; i < length; ++i) {
        result += static_cast<char>(buffer[position++]);
    }
    return result;
}

Styx::Size BufferReaderImpl::read(uint8_t* data, Styx::Size count)
{
    if (position + count > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    for (Styx::Size i = 0; i < count; ++i) {
        data[i] = buffer[position++];
    }
    return count;
}