#pragma once
#include "serialization/IBufferReader.h"

class BufferReaderImpl : public IBufferReader {
private:
    StyxBuffer buffer;
    Styx::Size position;
    Styx::Size limit;
public:
    BufferReaderImpl(const StyxBuffer buf, Styx::Size size);
    virtual ~BufferReaderImpl();
    uint8_t readUInt8() override;
    uint16_t readUInt16() override;
    uint32_t readUInt32() override;
    uint64_t readUInt64() override;
    StyxString readUTFString() override;
    Styx::Size read(uint8_t* data, Styx::Size count) override;
};
