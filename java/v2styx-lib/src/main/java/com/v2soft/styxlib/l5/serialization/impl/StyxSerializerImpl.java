package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.Checks;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferWritter;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.UTF;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.l6.StyxFile;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StyxSerializerImpl implements IDataSerializer {
    protected int getMessageSize(StyxMessage message) {
        var size = IDataSerializer.BASE_BINARY_SIZE;
        if (message instanceof StyxTMessageFID) {
            size += 4;
        }
        if (message instanceof StyxRSingleQIDMessage) {
            size += getQidSize();
        }
        switch (message.getType()) {
            case MessageType.Rerror -> size += UTF.getUTFSize(((StyxRErrorMessage)message).getError());
            case MessageType.Tattach -> {
                var attachMessage = (StyxTAttachMessage)message;
                size += 2 + 2 + UTF.getUTFSize(attachMessage.getUserName()) +
                        UTF.getUTFSize(attachMessage.getMountPoint());
            }
            case MessageType.Tauth -> {
                var authMessage = (StyxTAuthMessage)message;
                size += UTF.getUTFSize(authMessage.getUserName())
                    + UTF.getUTFSize(authMessage.getMountPoint());
            }
            case MessageType.Twalk -> {
                var walkMessage = (StyxTWalkMessage)message;
                size += 4 + 2;
                for (var pathElement : walkMessage.getPathElements())
                    size += UTF.getUTFSize(pathElement);
            }
            case MessageType.Topen -> size++;
            case MessageType.Tcreate -> {
                var createMessage = (StyxTCreateMessage)message;
                size += 5 + UTF.getUTFSize(createMessage.getName());
            }
            case MessageType.Twstat -> size += getStatSerializedSize(((StyxTWStatMessage)message).getStat());
            case MessageType.Twrite -> size += 12 + ((StyxTWriteMessage)message).getDataLength();
            case MessageType.Tread -> size += 8 + 4;
            case MessageType.Rwrite -> size += 4;
            case MessageType.Rstat -> size += 2 + getStatSerializedSize(((StyxRStatMessage)message).stat);
            case MessageType.Rread -> size += 4 + ((StyxRReadMessage)message).getDataLength();
            case MessageType.Tflush -> size += 2;
            case MessageType.Rcreate -> size += 4;
            case MessageType.Ropen -> size += 4;
            case MessageType.Tversion -> size += 4 + UTF.getUTFSize(((StyxTVersionMessage)message).getProtocolVersion());
            case MessageType.Rversion -> size += 4 + UTF.getUTFSize(((StyxRVersionMessage)message).protocolVersion);
            case MessageType.Rwalk -> {
                var walkMessage = (StyxRWalkMessage)message;
                size += 2 + walkMessage.getQIDListLength() * getQidSize();
            }
        }
        return size;
    }

    @Override
    public void serialize(StyxMessage message, IBufferWritter output) throws StyxException {
        int packetSize = getMessageSize(message);
        System.err.printf("ASD packetSize=%d %s", packetSize, message);
        output.prepareBuffer(packetSize);
        output.writeUInt32(packetSize);
        output.writeUInt8((short) message.getType());
        output.writeUInt16(message.getTag());
        if (Checks.isTMessage(message.getType())) {
            serializeTMessage(message, output);
        } else {
            serializeRMessage(message, output);
        }
    }

    private void serializeTMessage(StyxMessage message, IBufferWritter output) throws StyxException {
        if (message instanceof StyxTMessageFID) {
            StyxTMessageFID msg = (StyxTMessageFID) message;
            output.writeUInt32(msg.getFID());
        }
        switch (message.getType()) {
            case MessageType.Tversion:
                StyxTVersionMessage tVersionMessage = (StyxTVersionMessage) message;
                output.writeUInt32(tVersionMessage.getMaxPacketSize());
                output.writeUTFString(tVersionMessage.getProtocolVersion());
                break;
            case MessageType.Tcreate:
                StyxTCreateMessage tCreate = (StyxTCreateMessage) message;
                output.writeUTFString(tCreate.getName());
                output.writeUInt32(tCreate.getPermissions());
                output.writeUInt8((short) tCreate.getMode());
                break;
            case MessageType.Twalk:
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
            case MessageType.Twrite:
                StyxTWriteMessage tWriteMessage = (StyxTWriteMessage) message;
                output.writeUInt64(tWriteMessage.getOffset());
                output.writeUInt32(tWriteMessage.getDataLength());
                output.write(tWriteMessage.getData(), tWriteMessage.getDataOffset(), tWriteMessage.getDataLength());
                break;
            case MessageType.Tauth:
                StyxTAuthMessage tAuthMessage = (StyxTAuthMessage) message;
                output.writeUTFString(tAuthMessage.getUserName());
                output.writeUTFString(tAuthMessage.getMountPoint());
                break;
            case MessageType.Tread:
                StyxTReadMessage tReadMessage = (StyxTReadMessage) message;
                output.writeUInt64(tReadMessage.getOffset());
                output.writeUInt32(tReadMessage.getCount());
                break;
            case MessageType.Twstat:
                StyxTWStatMessage twStatMessage = (StyxTWStatMessage) message;
                output.writeUInt16(getStatSerializedSize(twStatMessage.getStat()));
                serializeStat(twStatMessage.getStat(), output);
                break;
            case MessageType.Tflush:
                StyxTFlushMessage tFlushMessage = (StyxTFlushMessage) message;
                output.writeUInt16(tFlushMessage.oldTag);
                break;
            case MessageType.Topen:
                StyxTOpenMessage tOpenMessage = (StyxTOpenMessage) message;
                output.writeUInt8((short) tOpenMessage.getMode());
                break;
            case MessageType.Tattach:
                StyxTAttachMessage tAttachMessage = (StyxTAttachMessage) message;
                output.writeUInt32(tAttachMessage.getAuthFID());
                output.writeUTFString(tAttachMessage.getUserName());
                output.writeUTFString(tAttachMessage.getMountPoint());
                break;
        }
    }

    private void serializeRMessage(StyxMessage message, IBufferWritter output) throws StyxException {
        if (message instanceof StyxRSingleQIDMessage) {
            StyxRSingleQIDMessage msg = (StyxRSingleQIDMessage) message;
            serializeQid(msg.getQID(), output);
        }
        switch (message.getType()) {
            case MessageType.Rerror:
                output.writeUTFString(((StyxRErrorMessage)message).getError());
                break;
            case MessageType.Rversion:
                StyxRVersionMessage version = (StyxRVersionMessage) message;
                output.writeUInt32(version.maxPacketSize);
                output.writeUTFString(version.protocolVersion);
                break;
            case MessageType.Rwrite:
                StyxRWriteMessage msg = (StyxRWriteMessage) message;
                output.writeUInt32(msg.count);
                break;
            case MessageType.Rread:
                StyxRReadMessage read = (StyxRReadMessage) message;
                output.writeUInt32(read.getDataLength());
                if ( read.getDataLength() > 0 ) {
                    output.write(read.getDataBuffer(), 0, read.getDataLength());
                }
                break;
            case MessageType.Rstat:
                StyxRStatMessage rStatMessage = (StyxRStatMessage) message;
                output.writeUInt16(getStatSerializedSize(rStatMessage.stat));
                serializeStat(rStatMessage.stat, output);
                break;
            case MessageType.Rcreate:
            case MessageType.Ropen:
                StyxROpenMessage rOpenMessage = (StyxROpenMessage) message;
                output.writeUInt32(rOpenMessage.ioUnit);
                break;
            case MessageType.Rwalk:
                StyxRWalkMessage rWalkMessage = (StyxRWalkMessage) message;
                output.writeUInt16(rWalkMessage.getQIDListLength());
                for (var qid : rWalkMessage.qidList)
                    serializeQid(qid, output);
                break;
        }
    }

    private long DateToInt(Date date) {
        if (date == null)
            return 0;
        return date.getTime() / 1000;
    }

    @Override
    public void serializeStat(StyxStat stat, IBufferWritter output)
            throws StyxException {
        int size = getStatSerializedSize(stat);
        output.writeUInt16(size - 2); // total size except first 2 bytes with size
        output.writeUInt16(stat.type());
        output.writeUInt32(stat.dev());
        serializeQid(stat.QID(), output);
//        stat.QID().writeBinaryTo(output);
        output.writeUInt32(stat.mode());
        output.writeUInt32(DateToInt(stat.accessTime()));
        output.writeUInt32(DateToInt(stat.modificationTime()));
        output.writeUInt64(stat.length());
        output.writeUTFString(stat.name());
        output.writeUTFString(stat.userName());
        output.writeUTFString(stat.groupName());
        output.writeUTFString(stat.modificationUser());
    }

    @Override
    public int getStatSerializedSize(StyxStat stat) {
        return 28 + getQidSize()
                + UTF.getUTFSize(stat.name())
                + UTF.getUTFSize(stat.userName())
                + UTF.getUTFSize(stat.groupName())
                + UTF.getUTFSize(stat.modificationUser());
    }

    @Override
    public int getQidSize() {
        return 13;
    }

    @Override
    public void serializeQid(StyxQID qid, IBufferWritter output) throws StyxException {
        output.writeUInt8((short) qid.type());
        output.writeUInt32(qid.version());
        output.writeUInt64(qid.path());
    }

    public static List<String> splitPath(String path) {
        if (path == null) {
            throw new NullPointerException("Path is null");
        }
        var result = new LinkedList<String>();
        if (!path.isEmpty()) {
            StringBuilder builder = new StringBuilder(path);
            while (builder.toString().startsWith(StyxFile.SEPARATOR))
                builder.delete(0, 1);
            while (builder.toString().endsWith(StyxFile.SEPARATOR))
                builder.delete(builder.length() - 1, builder.length());
            String [] pathElements = builder.toString().split(StyxFile.SEPARATOR);
            result.addAll(Arrays.asList(pathElements));
        }
        return result;
    }
}
