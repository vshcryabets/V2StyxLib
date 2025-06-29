#include "serialization/StyxSerializerImpl.h"


void StyxSerializerImpl::serialize(const styxlib::messages::StyxMessage &message, 
    IBufferWriter &output) {

    }

void StyxSerializerImpl::serializeStat(const StyxStat &stat, IBufferWriter &output)
{
    size_t size = getStatSerializedSize(stat);
    output.writeUInt16(size - 2); // total size except first 2 bytes with size
    output.writeUInt16(stat.type);
    output.writeUInt32(stat.dev);
    serializeQid(stat.QID, output);
    output.writeUInt32(stat.mode);
    output.writeUInt32(stat.accessTime);
    output.writeUInt32(stat.modificationTime);
    output.writeUInt64(stat.length);
    output.writeUTFString(stat.name);
    output.writeUTFString(stat.userName);
    output.writeUTFString(stat.groupName);
    output.writeUTFString(stat.modificationUser);
}

int StyxSerializerImpl::getStatSerializedSize(const StyxStat &stat)
{
    return 28 + getQidSize()
        + 2 + stat.name.length()
        + 2 + stat.userName.length()
        + 2 + stat.groupName.length()
        + 2 + stat.modificationUser.length();
}

int StyxSerializerImpl::getQidSize()
{
    return 13; // Size of StyxQID: 1 byte type, 4 bytes version, 8 bytes path
}

void StyxSerializerImpl::serializeQid(const StyxQID &qid, IBufferWriter &output)
{
    output.writeUInt8(qid.type);
    output.writeUInt32(qid.version);
    output.writeUInt64(qid.path);
}