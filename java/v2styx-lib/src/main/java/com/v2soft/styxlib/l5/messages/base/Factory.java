package com.v2soft.styxlib.l5.messages.base;

public interface Factory {
    StyxMessage constructTVersion(long iounit, String version);
    StyxMessage constructTAuth(long fid, String userName, String mountPoint);
    StyxMessage constructTAttach(long fid, long afid, String userName, String mountPoint);
}
