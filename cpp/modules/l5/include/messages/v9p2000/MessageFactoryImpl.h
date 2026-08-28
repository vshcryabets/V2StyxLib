#pragma once

#include "messages/base/MessageFactory.h"

namespace styxlib::messages::v9p2000
{

    using MessageFactory = styxlib::messages::base::MessageFactory;
    using StyxMessageUniquePtr = styxlib::StyxMessageUPtr;
    using QID = styxlib::structs::QID;
    using StyxStat = styxlib::structs::StyxStat;

    class MessageFactoryImpl : public MessageFactory
    {
    public:
        MessageFactoryImpl() = default;
        StyxMessageUniquePtr constructRerror(Tag tag, const StyxString &error) const override;
        StyxMessageUniquePtr constructRVersion(Tag tag, long maxPacketSize, const StyxString &protocolVersion) const override;
        StyxMessageUniquePtr constructRAttachMessage(Tag tag, const QID &qid) const override;
        StyxMessageUniquePtr constructRAuthMessage(Tag tag, const QID &qid) const override;
        StyxMessageUniquePtr constructROpenMessage(Tag tag, const QID &qid, long iounit) const override;
        StyxMessageUniquePtr constructRCreateMessage(Tag tag, const QID &qid, long iounit) const override;
        StyxMessageUniquePtr constructRStatMessage(Tag tag, const StyxStat &stat) const override;
        StyxMessageUniquePtr constructRWriteMessage(Tag tag, long count) const override;
        StyxMessageUniquePtr constructRWalkMessage(Tag tag, const std::vector<QID> &empty) const override;
        StyxMessageUniquePtr constructRReadMessage(Tag tag, StyxBuffer data, int size) const override;
        StyxMessageUniquePtr constructRClunk(Tag tag, long fid) const override;
        StyxMessageUniquePtr constructRRemove(int tag) const override;
        StyxMessageUniquePtr constructRWStat(int tag) const override;
        StyxMessageUniquePtr constructRFlush(int tag) const override;

        StyxMessageUniquePtr constructTVersion(long iounit, const StyxString &version) const override;
        StyxMessageUniquePtr constructTAuth(long fid, const StyxString &userName, const StyxString &mountPoint) const override;
        StyxMessageUniquePtr constructTAttach(long fid, long afid, const StyxString &userName, const StyxString &mountPoint) const override;
        StyxMessageUniquePtr constructTWriteMessage(long fid, long fileOffset, StyxBuffer data, int dataOffset, int dataLength) const override;
        StyxMessageUniquePtr constructTWalkMessage(long fid, long new_fid, const std::vector<StyxString> &path) const override;
        StyxMessageUniquePtr constructTWStatMessage(long fid, const StyxStat &stat) const override;
        StyxMessageUniquePtr constructTFlushMessage(int tag) const override;
        StyxMessageUniquePtr constructTOpenMessage(long fid, int mode) const override;
        StyxMessageUniquePtr constructTReadMessage(long fid, long offset, int count) const override;
        StyxMessageUniquePtr constructTCreateMessage(long fid, StyxString name, long permissions, int mode) const override;
        StyxMessageUniquePtr constructTClunk(long fid) const override;
        StyxMessageUniquePtr constructTRemove(long fid) const override;
        StyxMessageUniquePtr constructTStat(long fid) const override;
    };

}