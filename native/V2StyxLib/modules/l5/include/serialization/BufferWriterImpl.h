#pragma once

#include "serialization/IBufferWriter.h"

class BufferWriterImpl : public IBufferWriter {
protected:
    StyxBuffer buffer;
    Styx::Size position;
    Styx::Size limit;
public:
    BufferWriterImpl(size_t bufferSize);
    virtual ~BufferWriterImpl();
    void writeUInt8(uint8_t val) override;
    void writeUInt16(uint16_t val) override;
    void writeUInt32(uint32_t val) override;
    void writeUInt64(uint64_t value) override;
    void writeUTFString(StyxString string) override;
    Styx::Size write(const uint8_t* data, Styx::Size count) override;
    void prepareBuffer(Styx::Size bufferSize) override;
    StyxBuffer getBuffer() const override;
    Styx::Size getPosition() const override { return position; }
    Styx::Size getLimit() const override { return limit; }
};