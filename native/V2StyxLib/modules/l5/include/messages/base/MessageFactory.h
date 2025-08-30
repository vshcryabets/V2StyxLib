#pragma once
#include "StyxMessage.h"
#include <memory>
#include "structs/StyxQID.h"
#include "structs/StyxStat.h"
#include <vector>

namespace styxlib::messages::base
{

    class MessageFactory
    {
    public:
        virtual std::unique_ptr<StyxMessage> constructTVersion(long iounit, const StyxString &version) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTAuth(long fid, const StyxString &userName, const StyxString &mountPoint) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTAttach(long fid, long afid, const StyxString &userName, const StyxString &mountPoint) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRerror(int tag, const StyxString &error) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRVersion(long maxPacketSize, const StyxString &protocolVersion) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRAttachMessage(int tag, const StyxQID &qid) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRAuthMessage(int tag, const StyxQID &qid) const = 0;
        virtual std::unique_ptr<StyxMessage> constructROpenMessage(int tag, const StyxQID &qid, long iounit) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRCreateMessage(int tag, const StyxQID &qid, long iounit) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTWriteMessage(long fid, long fileOffset, std::unique_ptr<uint8_t[]> data, int dataOffset, int dataLength) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTWalkMessage(long fid, long new_fid, const std::vector<StyxString> &path) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTWStatMessage(long fid, const StyxStat &stat) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRStatMessage(int tag, const StyxStat &stat) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTFlushMessage(int tag) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTOpenMessage(long fid, int mode) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRWriteMessage(int tag, long count) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTReadMessage(long fid, long offset, int count) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRWalkMessage(int tag, const std::vector<StyxQID> &empty) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRReadMessage(int tag, std::unique_ptr<uint8_t[]> data, int size) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTCreateMessage(long fid, StyxString name, long permissions, int mode) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTClunk(long fid) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRClunk(int tag, long fid) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTRemove(long fid) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRRemove(int tag) const = 0;
        virtual std::unique_ptr<StyxMessage> constructTStat(long fid) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRWStat(int tag) const = 0;
        virtual std::unique_ptr<StyxMessage> constructRFlush(int tag) const = 0;
    };

}