#pragma once

#include <vector>

#include "messages/base/StyxMessage.h"
#include "structs/StyxQID.h"
#include "structs/StyxStat.h"

namespace styxlib::messages::base
{
    using QID = styxlib::structs::QID;
    using StyxStat = styxlib::structs::StyxStat;

    class MessageFactory
    {
    public:
        virtual StyxMessageUPtr constructTVersion(long iounit, const StyxString &version) const = 0;
        virtual StyxMessageUPtr constructTAuth(long fid, const StyxString &userName, const StyxString &mountPoint) const = 0;
        virtual StyxMessageUPtr constructTAttach(long fid, long afid, const StyxString &userName, const StyxString &mountPoint) const = 0;
        virtual StyxMessageUPtr constructRerror(Tag tag, const StyxString &error) const = 0;
        virtual StyxMessageUPtr constructRVersion(Tag tag, long maxPacketSize, const StyxString &protocolVersion) const = 0;
        virtual StyxMessageUPtr constructRAttachMessage(Tag tag, const QID &qid) const = 0;
        virtual StyxMessageUPtr constructRAuthMessage(Tag tag, const QID &qid) const = 0;
        virtual StyxMessageUPtr constructROpenMessage(Tag tag, const QID &qid, long iounit) const = 0;
        virtual StyxMessageUPtr constructRCreateMessage(Tag tag, const QID &qid, long iounit) const = 0;
        virtual StyxMessageUPtr constructTWriteMessage(long fid, long fileOffset, StyxBuffer data, int dataOffset, int dataLength) const = 0;
        virtual StyxMessageUPtr constructTWalkMessage(long fid, long new_fid, const std::vector<StyxString> &path) const = 0;
        virtual StyxMessageUPtr constructTWStatMessage(long fid, const StyxStat &stat) const = 0;
        virtual StyxMessageUPtr constructRStatMessage(Tag tag, const StyxStat &stat) const = 0;
        virtual StyxMessageUPtr constructTFlushMessage(int tag) const = 0;
        virtual StyxMessageUPtr constructTOpenMessage(long fid, int mode) const = 0;
        virtual StyxMessageUPtr constructRWriteMessage(Tag tag, long count) const = 0;
        virtual StyxMessageUPtr constructTReadMessage(long fid, long offset, int count) const = 0;
        virtual StyxMessageUPtr constructRWalkMessage(Tag tag, const std::vector<QID> &empty) const = 0;
        virtual StyxMessageUPtr constructRReadMessage(Tag tag, StyxBuffer data, int size) const = 0;
        virtual StyxMessageUPtr constructTCreateMessage(long fid, StyxString name, long permissions, int mode) const = 0;
        virtual StyxMessageUPtr constructTClunk(long fid) const = 0;
        virtual StyxMessageUPtr constructRClunk(Tag tag, long fid) const = 0;
        virtual StyxMessageUPtr constructTRemove(long fid) const = 0;
        virtual StyxMessageUPtr constructRRemove(int tag) const = 0;
        virtual StyxMessageUPtr constructTStat(long fid) const = 0;
        virtual StyxMessageUPtr constructRWStat(int tag) const = 0;
        virtual StyxMessageUPtr constructRFlush(int tag) const = 0;
    };

}