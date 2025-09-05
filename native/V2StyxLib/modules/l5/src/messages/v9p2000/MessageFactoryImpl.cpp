#include "messages/v9p2000/MessageFactoryImpl.h"
#include "messages/v9p2000/BaseMessage.h"
#include "messages/v9p2000/StyxTAuthMessage.h"
#include "messages/v9p2000/StyxTAttachMessage.h"
#include "enums/MessageType.h"
#include "enums/Constants.h"

namespace styxlib::messages::v9p2000
{
    using StyxMessage = styxlib::messages::base::StyxMessage;
    using StyxMessageUPtr = styxlib::messages::base::StyxMessageUPtr;

    StyxMessageUPtr MessageFactoryImpl::constructTVersion(long iounit, const StyxString &version) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tversion,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            0,
            iounit,
            version);
    }

    StyxMessageUPtr MessageFactoryImpl::constructTAuth(long fid, const StyxString &userName, const StyxString &mountPoint) const
    {
        return std::make_unique<StyxTAuthMessage>(
            fid,
            userName,
            mountPoint);
    }

    StyxMessageUPtr MessageFactoryImpl::constructTAttach(long fid, long afid, const StyxString &userName, const StyxString &mountPoint) const
    {
        return std::make_unique<StyxTAttachMessage>(
            fid,
            afid,
            userName,
            mountPoint);
    }

    StyxMessageUPtr MessageFactoryImpl::constructRerror(int tag, const StyxString &error) const
    {
        return std::make_unique<StyxRErrorMessage>(
            tag,
            error);
    }
    StyxMessageUPtr MessageFactoryImpl::constructRVersion(long maxPacketSize, const StyxString &protocolVersion) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rversion,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            0,
            maxPacketSize,
            protocolVersion);
    }
    StyxMessageUPtr MessageFactoryImpl::constructRAttachMessage(int tag, const QID &qid) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rattach,
            tag,
            qid,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRAuthMessage(int tag, const QID &qid) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rauth,
            tag,
            qid,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructROpenMessage(int tag, const QID &qid, long iounit) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Ropen,
            tag,
            qid,
            0,
            iounit,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRCreateMessage(int tag, const QID &qid, long iounit) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rcreate,
            tag,
            qid,
            0,
            iounit,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTWriteMessage(long fid, long fileOffset, StyxBuffer data, int dataOffset, int dataLength) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Twrite,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTWalkMessage(long fid, long new_fid, const std::vector<StyxString> &path) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Twalk,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTWStatMessage(long fid, const StyxStat &stat) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Twstat,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRStatMessage(int tag, const StyxStat &stat) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rstat,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTFlushMessage(int tag) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tflush,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTOpenMessage(long fid, int mode) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Topen,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRWriteMessage(int tag, long count) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rwrite,
            tag,
            QID::EMPTY,
            0,
            count,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTReadMessage(long fid, long offset, int count) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tread,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRWalkMessage(int tag, const std::vector<QID> &empty) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rwalk,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRReadMessage(int tag, StyxBuffer data, int size) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rread,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTCreateMessage(long fid, StyxString name, long permissions, int mode) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tcreate,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            name);
    }
    StyxMessageUPtr MessageFactoryImpl::constructTClunk(long fid) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tclunk,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRClunk(int tag, long fid) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rclunk,
            tag,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTRemove(long fid) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tremove,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRRemove(int tag) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rremove,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructTStat(long fid) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Tstat,
            styxlib::enums::NOTAG,
            QID::EMPTY,
            fid,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRWStat(int tag) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rwstat,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
    StyxMessageUPtr MessageFactoryImpl::constructRFlush(int tag) const
    {
        return std::make_unique<BaseMessage>(
            styxlib::enums::Rflush,
            tag,
            QID::EMPTY,
            0,
            0,
            "");
    }
}