#pragma once

#include "serialization/IBufferWriter.h"

using StyxSize = styxlib::Size;
using StyxBuffer = styxlib::StyxBuffer;

class BufferWriterImpl : public IBufferWriter
{
protected:
    StyxBuffer buffer;
    StyxSize position;
    StyxSize limit;

public:
    BufferWriterImpl(size_t bufferSize);
    virtual ~BufferWriterImpl();
    void writeUInt8(uint8_t val) override;
    void writeUInt16(uint16_t val) override;
    void writeUInt32(uint32_t val) override;
    void writeUInt64(uint64_t value) override;
    void writeUTFString(StyxString string) override;
    StyxSize write(const StyxBuffer data, StyxSize count) override;
    void prepareBuffer(StyxSize bufferSize) override;
    StyxBuffer getBuffer() const override;
    StyxSize getPosition() const override { return position; }
    StyxSize getLimit() const override { return limit; }
};