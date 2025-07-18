package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.messages.base.Factory;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;

public class FactoryImpl implements Factory  {
    @Override
    public StyxMessage constructTVersion(long iounit, String version) {
        return new StyxTVersionMessage(iounit, version);
    }

    @Override
    public StyxMessage constructTAuth(long fid, String userName, String mountPoint) {
        return new StyxTAuthMessage(fid, userName, mountPoint);
    }

    @Override
    public StyxMessage constructTAttach(long fid, long afid, String userName, String mountPoint) {
        return new StyxTAttachMessage(fid, afid, userName, mountPoint);
    }

    @Override
    public StyxMessage constructRerror(int tag, String error) {
        return new StyxRErrorMessage(tag, error);
    }
}
