#pragma once
#include "messages/base/StyxMessage.h"
#include "structs/StyxStat.h"
#include "serialization/IBufferWriter.h"

using StyxMessage = styxlib::messages::base::StyxMessage;

class IDataSerializer
{
public:
    static const size_t BASE_BINARY_SIZE = 7;

public:
    virtual Styx::Size getMessageSize(const StyxMessage &message) const = 0;
    virtual void serialize(const StyxMessage &message,
                           IBufferWriter &output) = 0;
    virtual void serializeStat(const StyxStat &stat, IBufferWriter &output) = 0;
    virtual Styx::Size getStatSerializedSize(const StyxStat &stat) = 0;
    virtual Styx::Size getQidSize() = 0;
    virtual void serializeQid(const StyxQID &qid, IBufferWriter &output) = 0;
};