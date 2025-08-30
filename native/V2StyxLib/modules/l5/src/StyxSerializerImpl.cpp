#include "serialization/StyxSerializerImpl.h"
#include "enums/MessageType.h"
#include "messages/v9p2000/BaseMessage.h"

using namespace styxlib::messages::v9p2000;
using namespace styxlib::messages::base;

void StyxSerializerImpl::serialize(const styxlib::messages::base::StyxMessage &message,
                                   IBufferWriter &output)
{
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

styxlib::Size StyxSerializerImpl::getStatSerializedSize(const StyxStat &stat)
{
    return 28 + getQidSize() + 2 + stat.name.length() + 2 + stat.userName.length() + 2 + stat.groupName.length() + 2 + stat.modificationUser.length();
}

styxlib::Size StyxSerializerImpl::getQidSize()
{
    return 13; // Size of StyxQID: 1 byte type, 4 bytes version, 8 bytes path
}

void StyxSerializerImpl::serializeQid(const styxlib::structs::QID &qid, IBufferWriter &output)
{
    output.writeUInt8(qid.type);
    output.writeUInt32(qid.version);
    output.writeUInt64(qid.path);
}

styxlib::Size StyxSerializerImpl::getMessageSize(const styxlib::messages::base::StyxMessage &message) const
{
    styxlib::Size size = IDataSerializer::BASE_BINARY_SIZE;
    // if (message instanceof StyxTMessageFID)
    // {
    //     size += 4;
    // }
    // if (message instanceof StyxRSingleQIDMessage)
    // {
    //     size += getQidSize();
    // }
    switch (message.getType())
    {
    case styxlib::enums::Rerror:
        size += ((const StyxRErrorMessage &)message).getMessage().length() + 2;
        break;
        // case styxlib::enums::Tattach:
        //     var attachMessage = (StyxTAttachMessage)message;
        //     size += 2 + 2 + UTF.getUTFSize(attachMessage.userName) +
        //             UTF.getUTFSize(attachMessage.mountPoint);
        //     break;

        // case styxlib::enums::Tauth:
        //     var authMessage = (StyxTAuthMessage)message;
        //     size += UTF.getUTFSize(authMessage.mUserName) + UTF.getUTFSize(authMessage.mMountPoint);
        //     break;
        // case styxlib::enums::Twalk:
        //     var walkMessage = (StyxTWalkMessage)message;
        //     size += 4 + 2;
        //     for (var pathElement : walkMessage.getPathElements())
        //         size += UTF.getUTFSize(pathElement);
        //     break;
        // case styxlib::enums::Topen:
        //     size++;
        //     break;
        // case styxlib::enums::Tcreate:

        //     var createMessage = (StyxTCreateMessage)message;
        //     size += 5 + UTF.getUTFSize(createMessage.name);
        //     break;
        // case styxlib::enums::Twstat:
        //     size += getStatSerializedSize(((StyxTWStatMessage)message).stat);
        //     break;
        // case styxlib::enums::Twrite:
        //     size += 12 + ((StyxTWriteMessage)message).dataLength;
        //     break;
        // case styxlib::enums::Tread:
        //     size += 8 + 4;
        //     break;
        // case styxlib::enums::Rwrite:
        //     size += 4;
        //     break;
        // case styxlib::enums::Rstat:
        //     size += 2 + getStatSerializedSize(((StyxRStatMessage)message).stat);
        //     break;
        // case styxlib::enums::Rread:
        //     size += 4 + ((StyxRReadMessage)message).dataLength;
        //     break;
        // case styxlib::enums::Tflush:
        //     size += 2;
        //     break;
        // case styxlib::enums::Rcreate:
        //     size += 4;
        //     break;
        // case styxlib::enums::Ropen:
        //     size += 4;
        //     break;
        // case styxlib::enums::Tversion:
        //     size += 4 + UTF.getUTFSize(((StyxTVersionMessage)message).protocolVersion);
        //     break;
        // case styxlib::enums::Rversion:
        //     size += 4 + UTF.getUTFSize(((StyxRVersionMessage)message).protocolVersion);
        //     break;
        // case styxlib::enums::Rwalk:

        //     var walkMessage = (StyxRWalkMessage)message;
        //     size += 2 + walkMessage.qidList.size() * getQidSize();
        //     break;
    }
    return size;
}