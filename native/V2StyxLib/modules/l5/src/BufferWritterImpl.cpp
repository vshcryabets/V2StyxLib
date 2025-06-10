#include "serialization/BufferWritterImpl.h"

BufferWritterImpl::BufferWritterImpl(size_t bufferSize) {
    mBuffer = new uint8_t[bufferSize];
    position = 0;
    limit = bufferSize;
}

BufferWritterImpl::~BufferWritterImpl() {
    delete[] mBuffer;
}

void BufferWritterImpl::writeUInt8(uint8_t val) {
    if (position + sizeof(uint8_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    mBuffer[position++] = val;
}
void BufferWritterImpl::writeUInt16(uint16_t val) {
    if (position + sizeof(uint16_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    mBuffer[position++] = static_cast<uint8_t>(val & 0xFF);
    mBuffer[position++] = static_cast<uint8_t>(val >> 8);
}
void BufferWritterImpl::writeUInt32(uint32_t val) {
    if (position + sizeof(uint32_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    mBuffer[position++] = static_cast<uint8_t>(val & 0xFF);
    mBuffer[position++] = static_cast<uint8_t>(val >> 8);
    mBuffer[position++] = static_cast<uint8_t>(val >> 16);
    mBuffer[position++] = static_cast<uint8_t>(val >> 24);
}
void BufferWritterImpl::writeUInt64(uint64_t value) {
    if (position + sizeof(uint64_t) > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    mBuffer[position++] = static_cast<uint8_t>(value & 0xFF);
    mBuffer[position++] = static_cast<uint8_t>(value >> 8);
    mBuffer[position++] = static_cast<uint8_t>(value >> 16);
    mBuffer[position++] = static_cast<uint8_t>(value >> 24);
    mBuffer[position++] = static_cast<uint8_t>(value >> 32);
    mBuffer[position++] = static_cast<uint8_t>(value >> 40);
    mBuffer[position++] = static_cast<uint8_t>(value >> 48);
    mBuffer[position++] = static_cast<uint8_t>(value >> 56);
}
void BufferWritterImpl::writeUTFString(StyxString string) {
    
}
Styx::Size BufferWritterImpl::write(const uint8_t* data, Styx::Size count) {
    if (position + count > limit) {
        throw std::out_of_range("Buffer overflow");
    }
    for (Styx::Size i = 0; i < count; ++i) {
        mBuffer[position++] = data[i];
    }
    return count;
}
void BufferWritterImpl::prepareBuffer(Styx::Size bufferSize) {
    
}
StyxBuffer BufferWritterImpl::getBuffer() const {
    return mBuffer;
}