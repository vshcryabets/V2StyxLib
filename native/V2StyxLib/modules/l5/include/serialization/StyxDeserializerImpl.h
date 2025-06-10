#pragma once

#include "l5/serialization/IDataSerializer.h"

class StyxDeserializerImpl : public IDataSerializer {
public:
    void serialize(const StyxMessage &message, IBufferWritter *output) override;

    void serializeStat(const StyxStat &stat, IBufferWritter *output) override;

    int getStatSerializedSize(const StyxStat &stat) override;

    int getQidSize() override;

    void serializeQid(const StyxQID &qid, IBufferWritter *output) override;
};