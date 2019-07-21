package com.v2soft.styxlib.messages.base;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.messages.base.structs.StyxQID;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxRSingleQIDMessage extends StyxMessage {
    protected StyxQID mQID;

    public StyxRSingleQIDMessage(MessageType type, int tag, StyxQID qid) {
        super(type, tag);
        mQID = qid;
    }

    /**
     * @return QID structure
     */
    public StyxQID getQID() {
        if (mQID == null) {
            return StyxQID.EMPTY;
        }
        return mQID;
    }

    public void setQID(StyxQID qid) {
        mQID = qid;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + StyxQID.CONTENT_SIZE;
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws IOException {
        super.writeToBuffer(output);
        getQID().writeBinaryTo(output);
    }

    @Override
    public String toString() {
        return String.format("%s\tQID: %s", super.toString(), getQID().toString());
    }

    @Override
    protected void load(IStyxDataReader buffer) throws IOException {
        super.load(buffer);
        mQID = new StyxQID(buffer);
    }
}
