#pragma once
#include <cstdint>
#include "data.h"

class IBufferWritter {
public:
    virtual ~IBufferWritter() = default;
    virtual void writeUInt8(uint8_t val) = 0;
    virtual void writeUInt16(uint16_t val) = 0;
    virtual void writeUInt32(uint32_t val) = 0;
    virtual void writeUInt64(uint64_t value) = 0;
    virtual void writeUTFString(StyxString string) = 0;
    virtual Styx::Size write(const uint8_t* data, Styx::Size count) = 0;
    // clean output and prepare to receieve data
    virtual void prepareBuffer(Styx::Size bufferSize) = 0;
    virtual StyxBuffer getBuffer() const = 0;
};