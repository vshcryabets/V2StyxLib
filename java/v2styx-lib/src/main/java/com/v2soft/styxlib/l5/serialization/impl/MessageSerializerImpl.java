package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.BufferWritter;
import com.v2soft.styxlib.l5.serialization.DataSerializer;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;
import java.util.Date;

public class MessageSerializerImpl implements DataSerializer {
    @Override
    public void serialize(StyxMessage message, BufferWritter output) throws IOException {
        int packetSize = message.getBinarySize();
        output.prepareBuffer(packetSize);
        output.writeUInt32(packetSize);
        output.writeUInt8((short) message.getType().getByte());
        output.writeUInt16(message.getTag());
        if (!message.getType().isTMessage()) {
            serializeRMessage(message, output);
        } else {
            serializeTMessage(message, output);
        }

    }

    private void serializeTMessage(StyxMessage message, BufferWritter output) throws IOException {
        if (message instanceof StyxTMessageFID) {
            StyxTMessageFID msg = (StyxTMessageFID) message;
            output.writeUInt32(msg.getFID());
        }
        switch (message.getType()) {
            case Tversion:
                StyxTVersionMessage tVersionMessage = (StyxTVersionMessage) message;
                output.writeUInt32(tVersionMessage.getMaxPacketSize());
                output.writeUTFString(tVersionMessage.getProtocolVersion());
                break;
            case Tcreate:
                StyxTCreateMessage tCreate = (StyxTCreateMessage) message;
                output.writeUTFString(tCreate.getName());
                output.writeUInt32(tCreate.getPermissions());
                output.writeUInt8((short) tCreate.getMode());
                break;
            case Twalk:
                StyxTWalkMessage tWalk = (StyxTWalkMessage) message;
                output.writeUInt32(tWalk.getNewFID());
                if (tWalk.getPathElements() != null) {
                    output.writeUInt16(tWalk.getPathElements().size());
                    for (String pathElement : tWalk.getPathElements())
                        output.writeUTFString(pathElement);
                } else {
                    output.writeUInt16(0);
                }
                break;
            case Twrite:
                StyxTWriteMessage tWriteMessage = (StyxTWriteMessage) message;
                output.writeUInt64(tWriteMessage.getOffset());
                output.writeUInt32(tWriteMessage.getDataLength());
                output.write(tWriteMessage.getData(), tWriteMessage.getDataOffset(), tWriteMessage.getDataLength());
                break;
            case Tauth:
                StyxTAuthMessage tAuthMessage = (StyxTAuthMessage) message;
                output.writeUTFString(tAuthMessage.getUserName());
                output.writeUTFString(tAuthMessage.getMountPoint());
                break;
            case Tread:
                StyxTReadMessage tReadMessage = (StyxTReadMessage) message;
                output.writeUInt64(tReadMessage.getOffset());
                output.writeUInt32(tReadMessage.getCount());
                break;
            case Twstat:
                StyxTWStatMessage twStatMessage = (StyxTWStatMessage) message;
                output.writeUInt16(twStatMessage.getStat().getSize());
                serializeStat(twStatMessage.getStat(), output);
                break;
            case Tflush:
                StyxTFlushMessage tFlushMessage = (StyxTFlushMessage) message;
                output.writeUInt16(tFlushMessage.getOldTag());
                break;
            case Topen:
                StyxTOpenMessage tOpenMessage = (StyxTOpenMessage) message;
                output.writeUInt8((short) tOpenMessage.getMode());
                break;
            case Tattach:
                StyxTAttachMessage tAttachMessage = (StyxTAttachMessage) message;
                output.writeUInt32(tAttachMessage.getAuthFID());
                output.writeUTFString(tAttachMessage.getUserName());
                output.writeUTFString(tAttachMessage.getMountPoint());
                break;
        }
    }

    private void serializeRMessage(StyxMessage message, BufferWritter output) throws IOException {
        if (message instanceof StyxRSingleQIDMessage) {
            StyxRSingleQIDMessage msg = (StyxRSingleQIDMessage) message;
            msg.getQID().writeBinaryTo(output);
        }
        switch (message.getType()) {
            case Rerror:
                output.writeUTFString(((StyxRErrorMessage)message).getError());
                break;
            case Rversion:
                StyxRVersionMessage version = (StyxRVersionMessage) message;
                output.writeUInt32(version.getMaxPacketSize());
                output.writeUTFString(version.getProtocolVersion());
                break;
            case Rwrite:
                StyxRWriteMessage msg = (StyxRWriteMessage) message;
                output.writeUInt32(msg.getCount());
                break;
            case Rread:
                StyxRReadMessage read = (StyxRReadMessage) message;
                output.writeUInt32(read.getDataLength());
                if ( read.getDataLength() > 0 ) {
                    output.write(read.getDataBuffer(), 0, read.getDataLength());
                }
                break;
            case Rstat:
                StyxRStatMessage rStatMessage = (StyxRStatMessage) message;
                output.writeUInt16(rStatMessage.getStat().getSize());
                serializeStat(rStatMessage.getStat(), output);
                break;
            case Ropen:
                StyxROpenMessage rOpenMessage = (StyxROpenMessage) message;
                output.writeUInt32(rOpenMessage.getIOUnit());
                break;
            case Rwalk:
                StyxRWalkMessage rWalkMessage = (StyxRWalkMessage) message;
                output.writeUInt16(rWalkMessage.getQIDListLength());
                for (StyxQID qid : rWalkMessage.getQIDIterable())
                    qid.writeBinaryTo(output);
                break;
        }
    }

    private long DateToInt(Date date) {
        if (date == null)
            return 0;
        return date.getTime() / 1000;
    }

    @Override
    public void serializeStat(StyxStat stat, BufferWritter output)
            throws IOException {
        int size = stat.getSize();
        output.writeUInt16(size - 2); // total size except first 2 bytes with size
        output.writeUInt16(stat.getType());
        output.writeUInt32(stat.getDev());
        stat.getQID().writeBinaryTo(output);
        output.writeUInt32(stat.getMode());
        output.writeUInt32(DateToInt(stat.getAccessTime()));
        output.writeUInt32(DateToInt(stat.getModificationTime()));
        output.writeUInt64(stat.getLength());
        output.writeUTFString(stat.getName());
        output.writeUTFString(stat.getUserName());
        output.writeUTFString(stat.getGroupName());
        output.writeUTFString(stat.getModificationUser());
    }
}
