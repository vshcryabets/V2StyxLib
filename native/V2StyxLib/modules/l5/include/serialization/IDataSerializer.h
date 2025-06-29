#pragma once
#include "messages/StyxMessage.h"
#include "structs/StyxStat.h"
#include "serialization/IBufferWriter.h"

class IDataSerializer {
public:
    virtual void serialize(const styxlib::messages::StyxMessage &message, 
        IBufferWriter &output) = 0;
    virtual void serializeStat(const StyxStat &stat, IBufferWriter &output) = 0;
    virtual int getStatSerializedSize(const StyxStat &stat) = 0;
    virtual int getQidSize() = 0;
    virtual void serializeQid(const StyxQID &qid, IBufferWriter &output) = 0;
};