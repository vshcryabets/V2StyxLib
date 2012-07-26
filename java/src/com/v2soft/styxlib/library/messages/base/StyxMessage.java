package com.v2soft.styxlib.library.messages.base;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.StyxROpenMessage;
import com.v2soft.styxlib.library.messages.StyxRReadMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
import com.v2soft.styxlib.library.messages.StyxRWriteMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
import com.v2soft.styxlib.library.messages.StyxTCreateMessage;
import com.v2soft.styxlib.library.messages.StyxTFlushMessage;
import com.v2soft.styxlib.library.messages.StyxTOpenMessage;
import com.v2soft.styxlib.library.messages.StyxTReadMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTWStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
import com.v2soft.styxlib.library.messages.StyxTWriteMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;

public class StyxMessage {
    // ========================================================
    // Constants
    // ========================================================
    public static final Charset sUTFCharset = Charset.forName("utf-8"); 
    public static final int BASE_BINARY_SIZE = 7;

    public static final int NOTAG  =      0xFFFF;
    public static final long NOFID = 0xFFFFFFFFL;
    // ========================================================
    // Class fields
    // ========================================================
    private int mTag;
    private MessageType mType;

    /**
     * Construct message from DoubleStateBuffer
     * @param is intput stream
     * @param io_unit packet size
     * @return constructed Message object
     * @throws IOException
     */
    public static StyxMessage factory(IStyxDataReader buffer, int io_unit) 
            throws IOException {
        // get common packet data
        long packet_size = buffer.readUInt32();
        if ( packet_size > io_unit ) {
            throw new IOException("Packet size to large");
        }
        MessageType type = MessageType.factory(buffer.readUInt8());
        if ( type == null ) {
            throw new NullPointerException("Type is null, can't decode message");
        }
        int tag = buffer.readUInt16();
        // load other data
        StyxMessage result = null;
        switch (type) {
        case Tversion:
            result = new StyxTVersionMessage(0, null);
            break;
        case Rversion:
            result = new StyxRVersionMessage(0, null);
            break;
        case Tauth:
            result = new StyxTAuthMessage(NOFID);
            break;
        case Tflush:
            result = new StyxTFlushMessage(NOTAG);
            break;
        case Tattach:
            result = new StyxTAttachMessage(NOFID, NOFID, null, null);
            break;
        case Twalk:
            result = new StyxTWalkMessage(NOFID, NOFID, "");
            break;
        case Rauth:
            result = new StyxRAuthMessage(tag, StyxQID.EMPTY);
            break;
        case Rerror:
            result = new StyxRErrorMessage(tag, null);
            break;
        case Rflush:
            result = new StyxMessage(MessageType.Rflush, tag);
            break;
        case Rattach:
            result = new StyxRAttachMessage(tag, StyxQID.EMPTY);
            break;
        case Rwalk:
            result = new StyxRWalkMessage(tag, null); 
            break;
        case Topen:
            result = new StyxTOpenMessage(NOFID, ModeType.OREAD);
            break;
        case Ropen:
            result = new StyxROpenMessage(tag, null, 0, false);
            break;
        case Tcreate:
            result = new StyxTCreateMessage(NOFID, null, 0, ModeType.OWRITE);
            break;
        case Rcreate:
            result = new StyxROpenMessage(tag, null, 0, true);
            break;
        case Tread:
            result = new StyxTReadMessage(NOFID, null, 0);
            break;
        case Rread:
            result = new StyxRReadMessage(tag, null, 0);
            break;
        case Twrite:
            result = new StyxTWriteMessage(NOFID, null, null, 0, 0 );
            break;
        case Rwrite:
            result = new StyxRWriteMessage(tag, 0);
            break;
        case Tclunk:
            result = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, 0);
            break;
        case Rclunk:
            result = new StyxMessage(MessageType.Rclunk, tag);
            break;
        case Tremove:
            result = new StyxTMessageFID(MessageType.Tremove, MessageType.Rremove, tag);
            break;
        case Rremove:
            result = new StyxMessage(MessageType.Rremove, tag);
            break;
        case Tstat:
            result = new StyxTMessageFID(MessageType.Tstat, MessageType.Rstat, tag);
            break;
        case Rstat:
            result = new StyxRStatMessage(tag);
            break;
        case Twstat:
            result = new StyxTWStatMessage(NOFID, null);
            break;
        case Rwstat:
            result = new StyxMessage(MessageType.Rwstat, tag);
            break;
        }
        result.setTag((short) tag);
        result.load(buffer);
        return result;
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

    public static int getUTFSize(String utf) {
        if (utf == null)
            return 2;
        return 2 + countUTFBytes(utf);
    }

    public static int countUTFBytes(String utf)	{
        String test = new String(utf);
        byte[] data = null;
        try {
            data = test.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ( data == null ) {
            return 0;
        } else {
            return data.length;
        }
    }

    public StyxMessage(MessageType type, int tag) {
        mType = type;
        mTag = tag;
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

    protected void load(IStyxDataReader buffer) throws IOException {
    }
    public void writeToBuffer(IStyxDataWriter output)  
            throws UnsupportedEncodingException, IOException {
        output.clear();
        int packetSize = getBinarySize();
        output.limit(packetSize);
        output.writeUInt32(packetSize);
        output.writeUInt8((short) getType().getByte());
        output.writeUInt16(getTag());
    }

    @Override
    public String toString() {
        return String.format("Type: %s\nTag: %d", 
                getType().toString(), getTag());
    }
}