#pragma once
#include "messages/StyxMessage.h"
#include "structs/StyxStat.h"
#include "serialization/IBufferWriter.h"

class IDataSerializer
{
public:
    static const size_t BASE_BINARY_SIZE = 7;

public:
    virtual Styx::Size getMessageSize(const styxlib::messages::StyxMessage &message) const = 0;
    virtual void serialize(const styxlib::messages::StyxMessage &message,
                           IBufferWriter &output) = 0;
    virtual void serializeStat(const StyxStat &stat, IBufferWriter &output) = 0;
    virtual Styx::Size getStatSerializedSize(const StyxStat &stat) = 0;
    virtual Styx::Size getQidSize() = 0;
    virtual void serializeQid(const StyxQID &qid, IBufferWriter &output) = 0;
};