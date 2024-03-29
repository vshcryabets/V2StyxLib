package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.structs.StyxQID;

import java.io.IOException;

public class MessagesFactory {
    /**
     * Construct message from DoubleStateBuffer
     * @param buffer input buffer
     * @param io_unit packet size
     * @return constructed Message object
     * @throws IOException in case of parse error.
     */
    public StyxMessage factory(BufferReader buffer, int io_unit)
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
                result = new StyxTAuthMessage(StyxMessage.NOFID, "", "");
                break;
            case Tflush:
                result = new StyxTFlushMessage(StyxMessage.NOTAG);
                break;
            case Tattach:
                result = new StyxTAttachMessage(StyxMessage.NOFID, StyxMessage.NOFID, null, null);
                break;
            case Twalk:
                result = new StyxTWalkMessage(StyxMessage.NOFID, StyxMessage.NOFID, "");
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
                result = new StyxTOpenMessage(StyxMessage.NOFID, ModeType.OREAD);
                break;
            case Ropen:
                result = new StyxROpenMessage(tag, null, 0, false);
                break;
            case Tcreate:
                result = new StyxTCreateMessage(StyxMessage.NOFID, null, 0, ModeType.OWRITE);
                break;
            case Rcreate:
                result = new StyxROpenMessage(tag, null, 0, true);
                break;
            case Tread:
                result = new StyxTReadMessage(StyxMessage.NOFID, 0, 0);
                break;
            case Rread:
                result = new StyxRReadMessage(tag, null, 0);
                break;
            case Twrite:
                result = new StyxTWriteMessage(StyxMessage.NOFID, 0, null, 0, 0 );
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
                result = new StyxTWStatMessage(StyxMessage.NOFID, null);
                break;
            case Rwstat:
                result = new StyxMessage(MessageType.Rwstat, tag);
                break;
        }
        result.setTag((short) tag);
        result.load(buffer);
        return result;
    }
}
