#pragma once
#include <cstdint>
#include "data.h"

namespace styxlib::serialization {
    
    class IBufferReader {
    public:
        virtual ~IBufferReader() = default;
        virtual uint8_t readUInt8() = 0;
        virtual uint16_t readUInt16() = 0;
        virtual uint32_t readUInt32() = 0;
        virtual uint64_t readUInt64() = 0;
        virtual StyxString readUTFString() = 0;
        virtual Styx::Size read(uint8_t* data, Styx::Size count) = 0;
    };
}