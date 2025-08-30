#pragma once
#include "serialization/IBufferReader.h"

class BufferReaderImpl : public styxlib::serialization::IBufferReader
{
private:
    const styxlib::StyxBuffer buffer;
    styxlib::Size position;
    styxlib::Size limit;

public:
    BufferReaderImpl(const styxlib::StyxBuffer &buf, styxlib::Size size);
    virtual ~BufferReaderImpl();
    uint8_t readUInt8() override;
    uint16_t readUInt16() override;
    uint32_t readUInt32() override;
    uint64_t readUInt64() override;
    styxlib::StyxString readUTFString() override;
    styxlib::Size read(uint8_t *data, styxlib::Size count) override;
};
