package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.messages.base.Factory;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.util.List;

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

    @Override
    public StyxMessage constructRVersion(long maxPacketSize, String protocolVersion) {
        return new StyxRVersionMessage(maxPacketSize, protocolVersion);
    }

    @Override
    public StyxMessage constructRAttachMessage(int tag, StyxQID qid) {
        return new StyxRAttachMessage(tag, qid);
    }

    @Override
    public StyxMessage constructRAuthMessage(int tag, StyxQID qid) {
        return new StyxRAuthMessage(tag, qid);
    }

    @Override
    public StyxMessage constructROpenMessage(int tag, StyxQID qid, long iounit) {
        return new StyxROpenMessage(tag, qid, iounit, false);
    }

    @Override
    public StyxMessage constructRCreateMessage(int tag, StyxQID qid, long iounit) {
        return new StyxROpenMessage(tag, qid, iounit, true);
    }

    @Override
    public StyxMessage constructTWriteMessage(long fid, long fileOffset, byte[] data, int dataOffset, int dataLength) {
        return new StyxTWriteMessage(fid, fileOffset, data, dataOffset, dataLength);
    }

    @Override
    public StyxMessage constructTWalkMessage(long fid, long new_fid, List<String> path) {
        return new StyxTWalkMessage(fid, new_fid, path);
    }

    @Override
    public StyxMessage constructTWStatMessage(long fid, StyxStat stat) {
        return new StyxTWStatMessage(fid, stat);
    }

    @Override
    public StyxMessage constructRStatMessage(int tag, StyxStat stat) {
        return new StyxRStatMessage(tag, stat);
    }

    @Override
    public StyxMessage constructTFlushMessage(int tag) {
        return new StyxTFlushMessage(tag);
    }

    @Override
    public StyxMessage constructTOpenMessage(long fid, int mode) {
        return new StyxTOpenMessage(fid, mode);
    }

    @Override
    public StyxMessage constructRWriteMessage(int tag, long count) {
        return new StyxRWriteMessage(tag, count);
    }

    @Override
    public StyxMessage constructTReadMessage(long fid, long offset, int count) {
        return new StyxTReadMessage(fid, offset, count);
    }

    @Override
    public StyxMessage constructRWalkMessage(int tag, List<StyxQID> empty) {
        return new StyxRWalkMessage(tag, empty);
    }

    @Override
    public StyxMessage constructRReadMessage(int tag, byte[] data, int size) {
        return new StyxRReadMessage(tag, data, size);
    }

    @Override
    public StyxMessage constructTCreateMessage(long fid, String name, long permissions, int mode) {
        return new StyxTCreateMessage(fid, name, permissions, mode);
    }
}
