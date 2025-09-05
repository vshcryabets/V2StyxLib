#pragma once

#include "messages/base/MessageFactory.h"

namespace styxlib::messages::v9p2000
{

    using MessageFactory = styxlib::messages::base::MessageFactory;
    using StyxMessageUniquePtr = styxlib::messages::base::StyxMessageUPtr;
    using QID = styxlib::structs::QID;

    class MessageFactoryImpl : public MessageFactory
    {
    public:
        MessageFactoryImpl() = default;
        StyxMessageUniquePtr constructRerror(int tag, const StyxString &error) const override;
        StyxMessageUniquePtr constructRVersion(int tag, long maxPacketSize, const StyxString &protocolVersion) const override;
        StyxMessageUniquePtr constructRAttachMessage(int tag, const QID &qid) const override;
        StyxMessageUniquePtr constructRAuthMessage(int tag, const QID &qid) const override;
        StyxMessageUniquePtr constructROpenMessage(int tag, const QID &qid, long iounit) const override;
        StyxMessageUniquePtr constructRCreateMessage(int tag, const QID &qid, long iounit) const override;
        StyxMessageUniquePtr constructRStatMessage(int tag, const StyxStat &stat) const override;
        StyxMessageUniquePtr constructRWriteMessage(int tag, long count) const override;
        StyxMessageUniquePtr constructRWalkMessage(int tag, const std::vector<QID> &empty) const override;
        StyxMessageUniquePtr constructRReadMessage(int tag, StyxBuffer data, int size) const override;
        StyxMessageUniquePtr constructRClunk(int tag, long fid) const override;
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