#pragma once

#include "serialization/IDataSerializer.h"

class StyxSerializerImpl : public IDataSerializer
{
public:
    StyxSerializerImpl() = default;
    ~StyxSerializerImpl() = default;
    Styx::Size getMessageSize(const styxlib::messages::StyxMessage &message) const override;
    void serialize(const styxlib::messages::StyxMessage &message,
                   IBufferWriter &output) override;
    void serializeStat(const StyxStat &stat, IBufferWriter &output) override;
    Styx::Size getStatSerializedSize(const StyxStat &stat) override;
    Styx::Size getQidSize() override;
    void serializeQid(const StyxQID &qid, IBufferWriter &output) override;
};