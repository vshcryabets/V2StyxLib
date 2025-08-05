package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.util.List;

public interface Factory {
    StyxMessage constructTVersion(long iounit, String version);
    StyxMessage constructTAuth(long fid, String userName, String mountPoint);
    StyxMessage constructTAttach(long fid, long afid, String userName, String mountPoint);
    StyxMessage constructRerror(int tag, String error);
    StyxMessage constructRVersion(long maxPacketSize, String protocolVersion);
    StyxMessage constructRAttachMessage(int tag, StyxQID qid);
    StyxMessage constructRAuthMessage(int tag, StyxQID qid);
    StyxMessage constructROpenMessage(int tag, StyxQID qid, long iounit);
    StyxMessage constructRCreateMessage(int tag, StyxQID qid, long iounit);
    StyxMessage constructTWriteMessage(long fid, long fileOffset, byte[] data, int dataOffset, int dataLength);
    StyxMessage constructTWalkMessage(long fid, long new_fid, List<String> path);
    StyxMessage constructTWStatMessage(long fid, StyxStat stat);
    StyxMessage constructRStatMessage(int tag, StyxStat stat);
    StyxMessage constructTFlushMessage(int tag);
    StyxMessage constructTOpenMessage(long fid, int mode);
    StyxMessage constructRWriteMessage(int tag, long count);
    StyxMessage constructTReadMessage(long fid, long offset, int count);
    StyxMessage constructRWalkMessage(int tag, List<StyxQID> empty);
    StyxMessage constructRReadMessage(int tag, byte[] data, int size);
    StyxMessage constructTCreateMessage(long fid, String name, long permissions, int mode);
}
