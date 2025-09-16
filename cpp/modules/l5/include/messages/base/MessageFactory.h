#pragma once
#include "StyxMessage.h"
#include <memory>
#include "structs/StyxQID.h"
#include "structs/StyxStat.h"
#include <vector>

namespace styxlib::messages::base
{
    using QID = styxlib::structs::QID;

    class MessageFactory
    {
    public:
        virtual StyxMessageUPtr constructTVersion(long iounit, const StyxString &version) const = 0;
        virtual StyxMessageUPtr constructTAuth(long fid, const StyxString &userName, const StyxString &mountPoint) const = 0;
        virtual StyxMessageUPtr constructTAttach(long fid, long afid, const StyxString &userName, const StyxString &mountPoint) const = 0;
        virtual StyxMessageUPtr constructRerror(int tag, const StyxString &error) const = 0;
        virtual StyxMessageUPtr constructRVersion(int tag, long maxPacketSize, const StyxString &protocolVersion) const = 0;
        virtual StyxMessageUPtr constructRAttachMessage(int tag, const QID &qid) const = 0;
        virtual StyxMessageUPtr constructRAuthMessage(int tag, const QID &qid) const = 0;
        virtual StyxMessageUPtr constructROpenMessage(int tag, const QID &qid, long iounit) const = 0;
        virtual StyxMessageUPtr constructRCreateMessage(int tag, const QID &qid, long iounit) const = 0;
        virtual StyxMessageUPtr constructTWriteMessage(long fid, long fileOffset, StyxBuffer data, int dataOffset, int dataLength) const = 0;
        virtual StyxMessageUPtr constructTWalkMessage(long fid, long new_fid, const std::vector<StyxString> &path) const = 0;
        virtual StyxMessageUPtr constructTWStatMessage(long fid, const StyxStat &stat) const = 0;
        virtual StyxMessageUPtr constructRStatMessage(int tag, const StyxStat &stat) const = 0;
        virtual StyxMessageUPtr constructTFlushMessage(int tag) const = 0;
        virtual StyxMessageUPtr constructTOpenMessage(long fid, int mode) const = 0;
        virtual StyxMessageUPtr constructRWriteMessage(int tag, long count) const = 0;
        virtual StyxMessageUPtr constructTReadMessage(long fid, long offset, int count) const = 0;
        virtual StyxMessageUPtr constructRWalkMessage(int tag, const std::vector<QID> &empty) const = 0;
        virtual StyxMessageUPtr constructRReadMessage(int tag, StyxBuffer data, int size) const = 0;
        virtual StyxMessageUPtr constructTCreateMessage(long fid, StyxString name, long permissions, int mode) const = 0;
        virtual StyxMessageUPtr constructTClunk(long fid) const = 0;
        virtual StyxMessageUPtr constructRClunk(int tag, long fid) const = 0;
        virtual StyxMessageUPtr constructTRemove(long fid) const = 0;
        virtual StyxMessageUPtr constructRRemove(int tag) const = 0;
        virtual StyxMessageUPtr constructTStat(long fid) const = 0;
        virtual StyxMessageUPtr constructRWStat(int tag) const = 0;
        virtual StyxMessageUPtr constructRFlush(int tag) const = 0;
    };

}