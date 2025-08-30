#pragma once

#include "serialization/IDataSerializer.h"

using StyxMessage = styxlib::messages::base::StyxMessage;

class StyxSerializerImpl : public IDataSerializer
{
public:
    StyxSerializerImpl() = default;
    ~StyxSerializerImpl() = default;
    styxlib::Size getMessageSize(const StyxMessage &message) const override;
    void serialize(const StyxMessage &message,
                   IBufferWriter &output) override;
    void serializeStat(const StyxStat &stat, IBufferWriter &output) override;
    styxlib::Size getStatSerializedSize(const StyxStat &stat) override;
    styxlib::Size getQidSize() override;
    void serializeQid(const styxlib::structs::QID &qid, IBufferWriter &output) override;
};