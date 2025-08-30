package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.util.List;

public class MessageFactoryImpl implements MessagesFactory {
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
        return new BaseMessage(MessageType.Rversion, Constants.NOTAG, null, 0, maxPacketSize, protocolVersion);
    }

    @Override
    public StyxMessage constructRAttachMessage(int tag, QID qid) {
        return new BaseMessage(MessageType.Rattach, tag, qid, 0, 0, null);
    }

    @Override
    public StyxMessage constructRAuthMessage(int tag, QID qid) {
        return new BaseMessage(MessageType.Rauth, tag, qid, 0, 0, null);
    }

    @Override
    public StyxMessage constructROpenMessage(int tag, QID qid, long iounit) {
        return new BaseMessage(MessageType.Ropen, tag, qid, 0, iounit, null);
    }

    @Override
    public StyxMessage constructRCreateMessage(int tag, QID qid, long iounit) {
        return new BaseMessage(MessageType.Rcreate, tag, qid, 0, iounit, null);
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
    public StyxMessage constructRWalkMessage(int tag, List<QID> empty) {
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

    @Override
    public StyxMessage constructTClunk(long fid) {
        return new BaseMessage(
                MessageType.Tclunk,
                0, // tag will be set later
                null, // no QID for Tclunk
                fid,
                0,
                null
        );
    }

    @Override
    public StyxMessage constructRClunk(int tag, long fid) {
        return new BaseMessage(
                MessageType.Rclunk,
                tag,
                null, // no QID for Rclunk
                fid,
                0,
                null
        );
    }

    @Override
    public StyxMessage constructTRemove(long fid) {
        return new BaseMessage(
                MessageType.Tremove,
                0,
                null,
                fid,
                0,
                null
        );
    }

    @Override
    public StyxMessage constructRRemove(int tag) {
        return new BaseMessage(
                MessageType.Rremove,
                tag,
                null, // no QID for Rremove
                0, // fid is not needed for Rremove
                0,
                null
        );
    }

    @Override
    public StyxMessage constructTStat(long fid) {
        return new BaseMessage(
                MessageType.Tstat,
                0,
                null,
                fid,
                0,
                null
        );
    }

    @Override
    public StyxMessage constructRWStat(int tag) {
        return new BaseMessage(
                MessageType.Rwstat,
                tag,
                null, // no QID for Rwstat
                0, // fid is not needed for Rwstat
                0,
                null
        );
    }

    @Override
    public StyxMessage constructRFlush(int tag) {
        return new BaseMessage(
                MessageType.Rflush,
                tag,
                null, // no QID for Rflush
                0, // fid is not needed for Rflush
                0,
                null
        );
    }
}
