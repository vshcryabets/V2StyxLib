#pragma once

#include "messages/base/MessageFactory.h"

namespace styxlib::messages::v9p2000
{

    using MessageFactory = styxlib::messages::base::MessageFactory;
    using StyxMessage = styxlib::messages::base::StyxMessage;
    using QID = styxlib::structs::QID;

    class MessageFactoryImpl : public MessageFactory
    {
    public:
        MessageFactoryImpl() = default;
        std::unique_ptr<StyxMessage> constructTVersion(long iounit, const StyxString &version) const override;
        std::unique_ptr<StyxMessage> constructTAuth(long fid, const StyxString &userName, const StyxString &mountPoint) const override;
        std::unique_ptr<StyxMessage> constructTAttach(long fid, long afid, const StyxString &userName, const StyxString &mountPoint) const override;
        std::unique_ptr<StyxMessage> constructRerror(int tag, const StyxString &error) const override;
        std::unique_ptr<StyxMessage> constructRVersion(long maxPacketSize, const StyxString &protocolVersion) const override;
        std::unique_ptr<StyxMessage> constructRAttachMessage(int tag, const QID &qid) const override;
        std::unique_ptr<StyxMessage> constructRAuthMessage(int tag, const QID &qid) const override;
        std::unique_ptr<StyxMessage> constructROpenMessage(int tag, const QID &qid, long iounit) const override;
        std::unique_ptr<StyxMessage> constructRCreateMessage(int tag, const QID &qid, long iounit) const override;
        std::unique_ptr<StyxMessage> constructTWriteMessage(long fid, long fileOffset, StyxBuffer data, int dataOffset, int dataLength) const override;
        std::unique_ptr<StyxMessage> constructTWalkMessage(long fid, long new_fid, const std::vector<StyxString> &path) const override;
        std::unique_ptr<StyxMessage> constructTWStatMessage(long fid, const StyxStat &stat) const override;
        std::unique_ptr<StyxMessage> constructRStatMessage(int tag, const StyxStat &stat) const override;
        std::unique_ptr<StyxMessage> constructTFlushMessage(int tag) const override;
        std::unique_ptr<StyxMessage> constructTOpenMessage(long fid, int mode) const override;
        std::unique_ptr<StyxMessage> constructRWriteMessage(int tag, long count) const override;
        std::unique_ptr<StyxMessage> constructTReadMessage(long fid, long offset, int count) const override;
        std::unique_ptr<StyxMessage> constructRWalkMessage(int tag, const std::vector<QID> &empty) const override;
        std::unique_ptr<StyxMessage> constructRReadMessage(int tag, StyxBuffer data, int size) const override;
        std::unique_ptr<StyxMessage> constructTCreateMessage(long fid, StyxString name, long permissions, int mode) const override;
        std::unique_ptr<StyxMessage> constructTClunk(long fid) const override;
        std::unique_ptr<StyxMessage> constructRClunk(int tag, long fid) const override;
        std::unique_ptr<StyxMessage> constructTRemove(long fid) const override;
        std::unique_ptr<StyxMessage> constructRRemove(int tag) const override;
        std::unique_ptr<StyxMessage> constructTStat(long fid) const override;
        std::unique_ptr<StyxMessage> constructRWStat(int tag) const override;
        std::unique_ptr<StyxMessage> constructRFlush(int tag) const override;
    };

}