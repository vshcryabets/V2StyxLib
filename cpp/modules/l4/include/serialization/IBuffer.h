#pragma once
#include <cstdint>
#include "dataL4.h"

namespace styxlib::serialization
{
    using StyxSize = styxlib::Size;
    using StyxString = styxlib::StyxString;
    using StyxBuffer = styxlib::StyxBuffer;

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

    class IBufferReader
    {
    public:
        virtual ~IBufferReader() = default;
        virtual uint8_t readUInt8() = 0;
        virtual uint16_t readUInt16() = 0;
        virtual uint32_t readUInt32() = 0;
        virtual uint64_t readUInt64() = 0;
        virtual StyxString readUTFString() = 0;
        virtual StyxSize read(uint8_t *data, StyxSize count) = 0;
    };
}