package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.dev.MetricsAndStats;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.QID;

public class BaseMessage implements StyxMessage {
    protected int tag;
    public final int type;
    protected final QID qid;
    protected long fid;
    protected long iounit;
    protected final String protocolVersion;

    protected BaseMessage(
            int type,
            int tag,
            QID qid,
            long fid,
            long iounit,
            String protocolVersion) {
        MetricsAndStats.newStyxMessage++;
        this.type = type;
        this.tag = tag;
        this.qid = qid;
        this.fid = fid;
        this.iounit = iounit;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = (tag & 0xFFFF);
    }

    @Override
    public int getType() {
        return type;
    }

    public QID getQID() {
        return qid;
    }

    public long getFID() {
        return fid;
    }
    public long getIounit() {
        return iounit;
    }
    public String getProtocolVersion() {
        return protocolVersion;
    }
}
