package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.util.List;

public interface MessagesFactory {
    StyxMessage constructRerror(int tag, String error);
    StyxMessage constructRVersion(int tag, long maxPacketSize, String protocolVersion);
    StyxMessage constructRAttachMessage(int tag, QID qid);
    StyxMessage constructRAuthMessage(int tag, QID qid);
    StyxMessage constructROpenMessage(int tag, QID qid, long iounit);
    StyxMessage constructRCreateMessage(int tag, QID qid, long iounit);
    StyxMessage constructRStatMessage(int tag, StyxStat stat);
    StyxMessage constructRWriteMessage(int tag, long count);
    StyxMessage constructRWalkMessage(int tag, List<QID> empty);
    StyxMessage constructRReadMessage(int tag, byte[] data, int size);
    StyxMessage constructRClunk(int tag, long fid);
    StyxMessage constructRRemove(int tag);
    StyxMessage constructRWStat(int tag);
    StyxMessage constructRFlush(int tag);


    StyxMessage constructTVersion(long iounit, String version);
    StyxMessage constructTAuth(long fid, String userName, String mountPoint);
    StyxMessage constructTAttach(long fid, long afid, String userName, String mountPoint);
    StyxMessage constructTWriteMessage(long fid, long fileOffset, byte[] data, int dataOffset, int dataLength);
    StyxMessage constructTWalkMessage(long fid, long new_fid, List<String> path);
    StyxMessage constructTWStatMessage(long fid, StyxStat stat);
    StyxMessage constructTFlushMessage(int tag);
    StyxMessage constructTOpenMessage(long fid, int mode);
    StyxMessage constructTReadMessage(long fid, long offset, int count);
    StyxMessage constructTCreateMessage(long fid, String name, long permissions, int mode);
    StyxMessage constructTClunk(long fid);
    StyxMessage constructTRemove(long fid);
    StyxMessage constructTStat(long fid);
}
