#pragma once

#include "serialization/IBufferWritter.h"

class BufferWritterImpl : public IBufferWritter {
protected:
    StyxBuffer mBuffer;
    Styx::Size position;
    Styx::Size limit;
public:
    BufferWritterImpl(size_t bufferSize);
    virtual ~BufferWritterImpl();
    void writeUInt8(uint8_t val) override;
    void writeUInt16(uint16_t val) override;
    void writeUInt32(uint32_t val) override;
    void writeUInt64(uint64_t value) override;
    void writeUTFString(StyxString string) override;
    Styx::Size write(const uint8_t* data, Styx::Size count) override;
    void prepareBuffer(Styx::Size bufferSize) override;
    StyxBuffer getBuffer() const override;
};