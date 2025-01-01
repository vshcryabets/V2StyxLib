package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
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
            case Rerror -> size += UTF.getUTFSize(((StyxRErrorMessage)message).getError());
            case Tattach -> {
                var attachMessage = (StyxTAttachMessage)message;
                size += 2 + 2 + UTF.getUTFSize(attachMessage.getUserName()) +
                        UTF.getUTFSize(attachMessage.getMountPoint());
            }
            case Tauth -> {
                var authMessage = (StyxTAuthMessage)message;
                size += UTF.getUTFSize(authMessage.getUserName())
                    + UTF.getUTFSize(authMessage.getMountPoint());
            }
            case Twalk -> {
                var walkMessage = (StyxTWalkMessage)message;
                size += 4 + 2;
                for (var pathElement : walkMessage.getPathElements())
                    size += UTF.getUTFSize(pathElement);
            }
            case Topen -> size++;
            case Tcreate -> {
                var createMessage = (StyxTCreateMessage)message;
                size += 5 + UTF.getUTFSize(createMessage.getName());
            }
            case Twstat -> size += getStatSerializedSize(((StyxTWStatMessage)message).getStat());
            case Twrite -> size += 12 + ((StyxTWriteMessage)message).getDataLength();
            case Tread -> size += 8 + 4;
            case Rwrite -> size += 4;
            case Rstat -> size += 2 + getStatSerializedSize(((StyxRStatMessage)message).stat);
            case Rread -> size += 4 + ((StyxRReadMessage)message).getDataLength();
            case Tflush -> size += 2;
            case Rcreate -> size += 4;
            case Ropen -> size += 4;
            case Tversion -> size += 4 + UTF.getUTFSize(((StyxTVersionMessage)message).getProtocolVersion());
            case Rversion -> size += 4 + UTF.getUTFSize(((StyxRVersionMessage)message).protocolVersion);
            case Rwalk -> {
                var walkMessage = (StyxRWalkMessage)message;
                size += 2 + walkMessage.getQIDListLength() * getQidSize();
            }
        }
        return size;
    }

    @Override
    public void serialize(StyxMessage message, IBufferWritter output) throws StyxException {
        int packetSize = getMessageSize(message);
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

    private void serializeTMessage(StyxMessage message, IBufferWritter output) throws StyxException {
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
                output.writeUInt16(getStatSerializedSize(twStatMessage.getStat()));
                serializeStat(twStatMessage.getStat(), output);
                break;
            case Tflush:
                StyxTFlushMessage tFlushMessage = (StyxTFlushMessage) message;
                output.writeUInt16(tFlushMessage.oldTag);
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

    private void serializeRMessage(StyxMessage message, IBufferWritter output) throws StyxException {
        if (message instanceof StyxRSingleQIDMessage) {
            StyxRSingleQIDMessage msg = (StyxRSingleQIDMessage) message;
            serializeQid(msg.getQID(), output);
        }
        switch (message.getType()) {
            case Rerror:
                output.writeUTFString(((StyxRErrorMessage)message).getError());
                break;
            case Rversion:
                StyxRVersionMessage version = (StyxRVersionMessage) message;
                output.writeUInt32(version.maxPacketSize);
                output.writeUTFString(version.protocolVersion);
                break;
            case Rwrite:
                StyxRWriteMessage msg = (StyxRWriteMessage) message;
                output.writeUInt32(msg.count);
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
                output.writeUInt16(getStatSerializedSize(rStatMessage.stat));
                serializeStat(rStatMessage.stat, output);
                break;
            case Rcreate:
            case Ropen:
                StyxROpenMessage rOpenMessage = (StyxROpenMessage) message;
                output.writeUInt32(rOpenMessage.ioUnit);
                break;
            case Rwalk:
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
        output.writeUInt8((short) qid.getType().getByte());
        output.writeUInt32(qid.getVersion());
        output.writeUInt64(qid.getPath());
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
