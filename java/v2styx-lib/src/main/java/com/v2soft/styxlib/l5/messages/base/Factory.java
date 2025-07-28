package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.structs.StyxQID;

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
}
