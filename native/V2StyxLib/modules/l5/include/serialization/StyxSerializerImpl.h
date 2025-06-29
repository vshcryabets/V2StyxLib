#pragma once

#include "serialization/IDataSerializer.h"

class StyxSerializerImpl : public IDataSerializer {
public:
    StyxSerializerImpl() = default;
    ~StyxSerializerImpl() = default;
    void serialize(const styxlib::messages::StyxMessage &message, 
        IBufferWriter &output) override;
    void serializeStat(const StyxStat &stat, IBufferWriter &output) override;
    int getStatSerializedSize(const StyxStat &stat) override;
    int getQidSize() override;
    void serializeQid(const StyxQID &qid, IBufferWriter &output) override;
};