#pragma once
#include "l5/messages/StyxMessage.h"
#include "l5/structs/StyxStat.h"
#include "l5/serialization/IBufferWritter.h"

class IDataSerializer {
public:
    virtual void serialize(const StyxMessage &message, IBufferWritter *output) = 0;
    virtual void serializeStat(const StyxStat &stat, IBufferWritter *output) = 0;
    virtual int getStatSerializedSize(const StyxStat &stat) = 0;
    virtual int getQidSize() = 0;
    virtual void serializeQid(const StyxQID &qid, IBufferWritter *output) = 0;
};