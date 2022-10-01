package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.io.IStyxDataReader;
import com.v2soft.styxlib.l5.io.IStyxDataWriter;
import com.v2soft.styxlib.l5.messages.StyxRReadMessage;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.messages.StyxRAttachMessage;
import com.v2soft.styxlib.l5.messages.StyxRAuthMessage;
import com.v2soft.styxlib.l5.messages.StyxRErrorMessage;
import com.v2soft.styxlib.l5.messages.StyxROpenMessage;
import com.v2soft.styxlib.l5.messages.StyxRStatMessage;
import com.v2soft.styxlib.l5.messages.StyxRVersionMessage;
import com.v2soft.styxlib.l5.messages.StyxRWalkMessage;
import com.v2soft.styxlib.l5.messages.StyxRWriteMessage;
import com.v2soft.styxlib.l5.messages.StyxTAttachMessage;
import com.v2soft.styxlib.l5.messages.StyxTAuthMessage;
import com.v2soft.styxlib.l5.messages.StyxTCreateMessage;
import com.v2soft.styxlib.l5.messages.StyxTFlushMessage;
import com.v2soft.styxlib.l5.messages.StyxTOpenMessage;
import com.v2soft.styxlib.l5.messages.StyxTReadMessage;
import com.v2soft.styxlib.l5.messages.StyxTVersionMessage;
import com.v2soft.styxlib.l5.messages.StyxTWStatMessage;
import com.v2soft.styxlib.l5.messages.StyxTWalkMessage;
import com.v2soft.styxlib.l5.messages.StyxTWriteMessage;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class StyxMessage {
    // ========================================================
    // Constants
    // ========================================================
    private static final int BASE_BINARY_SIZE = 7;
    public static final int NOTAG  =      0xFFFF;
    public static final long NOFID = 0xFFFFFFFFL;
    // ========================================================
    // Class fields
    // ========================================================
    private int mTag;
    private MessageType mType;

    public StyxMessage(MessageType type, int tag) {
        MetricsAndStats.newStyxMessage++;
        mType = type;
        mTag = tag;
    }

    public static String toString(byte[] bytes) {
        if ( (bytes == null) || (bytes.length==0))
            return "-";
        final StringBuilder result = new StringBuilder();
        result.append(Integer.toHexString(((int)bytes[0])&0xFF));
        result.append(",");
        int count = bytes.length;
        for (int i=1; i<count; i++)
        {
            result.append(Integer.toHexString(((int)bytes[i])&0xFF));
            result.append(',');
        }
        return String.format("(%s)", result);
    }

    public MessageType getType(){
        return mType;
    }

    public int getTag(){
        return mTag;
    }

    public void setTag(int tag){
        mTag = (tag & 0xFFFF);
    }

    public int getBinarySize(){
        return BASE_BINARY_SIZE;
    }

    public void load(IStyxDataReader buffer) throws IOException {
    }

    public void writeToBuffer(IStyxDataWriter output)
            throws IOException {
        output.clear();
        int packetSize = getBinarySize();
        output.limit(packetSize);
        output.writeUInt32(packetSize);
        output.writeUInt8((short) getType().getByte());
        output.writeUInt16(getTag());
    }

    @Override
    public String toString() {
        return String.format("Type: %s\tTag: %d",
                getType().toString(), getTag());
    }
}
