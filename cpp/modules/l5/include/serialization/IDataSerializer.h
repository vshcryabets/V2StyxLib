#pragma once
#include "messages/base/StyxMessage.h"
#include "structs/StyxStat.h"
#include "serialization/IBuffer.h"

using StyxMessage = styxlib::messages::base::StyxMessage;
using StyxStat = styxlib::structs::StyxStat;

class IDataSerializer
{
public:
    static const size_t BASE_BINARY_SIZE = 7;

public:
    virtual styxlib::Size getMessageSize(const StyxMessage &message) const = 0;
    virtual void serialize(const StyxMessage &message,
                           styxlib::serialization::IBufferWriter &output) = 0;
    virtual void serializeStat(const StyxStat &stat, styxlib::serialization::IBufferWriter &output) = 0;
    virtual styxlib::Size getStatSerializedSize(const StyxStat &stat) = 0;
    virtual styxlib::Size getQidSize() = 0;
    virtual void serializeQid(const styxlib::structs::QID &qid, styxlib::serialization::IBufferWriter &output) = 0;
};