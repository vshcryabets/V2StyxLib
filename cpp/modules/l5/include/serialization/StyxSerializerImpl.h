#pragma once

#include "serialization/IDataSerializer.h"

class StyxSerializerImpl : public IDataSerializer
{
public:
    StyxSerializerImpl() = default;
    ~StyxSerializerImpl() = default;
    styxlib::Size getMessageSize(const styxlib::messages::base::StyxMessage &message) const override;
    void serialize(const styxlib::messages::base::StyxMessage &message,
                   IBufferWriter &output) override;
    void serializeStat(const StyxStat &stat, IBufferWriter &output) override;
    styxlib::Size getStatSerializedSize(const StyxStat &stat) override;
    styxlib::Size getQidSize() override;
    void serializeQid(const styxlib::structs::QID &qid, IBufferWriter &output) override;
};