#pragma once
#include <cstdint>
#include "data.h"

using StyxString = styxlib::StyxString;
using StyxDate = styxlib::StyxDate;
using StyxBuffer = styxlib::StyxBuffer;
using StyxSize = styxlib::Size;

class IBufferWriter
{
public:
    virtual ~IBufferWriter() = default;
    virtual void writeUInt8(uint8_t val) = 0;
    virtual void writeUInt16(uint16_t val) = 0;
    virtual void writeUInt32(uint32_t val) = 0;
    virtual void writeUInt64(uint64_t value) = 0;
    virtual void writeUTFString(StyxString string) = 0;
    virtual StyxSize write(const StyxBuffer data, StyxSize count) = 0;
    // clean output and prepare to receieve data
    virtual void prepareBuffer(StyxSize bufferSize) = 0;
    virtual StyxBuffer getBuffer() const = 0;
    virtual StyxSize getPosition() const = 0;
    virtual StyxSize getLimit() const = 0;
};