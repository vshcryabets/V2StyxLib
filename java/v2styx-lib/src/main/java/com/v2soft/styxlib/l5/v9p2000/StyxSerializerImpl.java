package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.Checks;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.*;
import com.v2soft.styxlib.l5.serialization.IBufferWriter;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.UTF;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.l6.StyxFile;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StyxSerializerImpl implements IDataSerializer {
    private final Set<Integer> FID_MESSAGES = Set.of(
            MessageType.Tattach,
            MessageType.Tauth,
            MessageType.Tcreate,
            MessageType.Topen,
            MessageType.Tread,
            MessageType.Twalk,
            MessageType.Twrite,
            MessageType.Twstat,
            MessageType.Tstat,
            MessageType.Tclunk,
            MessageType.Tremove
    );

    @Override
    public int getMessageSize(StyxMessage message) {
        var size = IDataSerializer.BASE_BINARY_SIZE;
        if (FID_MESSAGES.contains(message.getType()) ||
                message.getType() == MessageType.Rclunk) {
            size += 4; // FID
        }
        if (message instanceof BaseMessage && ((BaseMessage) message).mQID != null) {
            size += getQidSize();
        }
        switch (message.getType()) {
            case MessageType.Rerror -> size += UTF.getUTFSize(((StyxRErrorMessage)message).mError);
            case MessageType.Tattach -> {
                var attachMessage = (StyxTAttachMessage)message;
                size += 2 + 2 + UTF.getUTFSize(attachMessage.userName) +
                        UTF.getUTFSize(attachMessage.mountPoint);
            }
            case MessageType.Tauth -> {
                var authMessage = (StyxTAuthMessage)message;
                size += UTF.getUTFSize(authMessage.mUserName)
                    + UTF.getUTFSize(authMessage.mMountPoint);
            }
            case MessageType.Twalk -> {
                var walkMessage = (StyxTWalkMessage)message;
                size += 4 + 2;
                for (var pathElement : walkMessage.mPathElements)
                    size += UTF.getUTFSize(pathElement);
            }
            case MessageType.Topen -> size++;
            case MessageType.Tcreate -> {
                var createMessage = (StyxTCreateMessage)message;
                size += 5 + UTF.getUTFSize(createMessage.name);
            }
            case MessageType.Twstat -> size += getStatSerializedSize(((StyxTWStatMessage)message).stat);
            case MessageType.Twrite -> size += 12 + ((StyxTWriteMessage)message).dataLength;
            case MessageType.Tread -> size += 8 + 4;
            case MessageType.Rwrite -> size += 4;
            case MessageType.Rstat -> size += 2 + getStatSerializedSize(((StyxRStatMessage)message).stat);
            case MessageType.Rread -> size += 4 + ((StyxRReadMessage)message).dataLength;
            case MessageType.Tflush -> size += 2;
            case MessageType.Rcreate -> size += 4;
            case MessageType.Ropen -> size += 4;
            case MessageType.Tversion -> size += 4 + UTF.getUTFSize(((StyxTVersionMessage)message).protocolVersion);
            case MessageType.Rversion -> size += 4 + UTF.getUTFSize(((StyxRVersionMessage)message).protocolVersion);
            case MessageType.Rwalk -> {
                var walkMessage = (StyxRWalkMessage)message;
                size += 2 + walkMessage.qidList.size() * getQidSize();
            }
        }
        return size;
    }

    @Override
    public void serialize(StyxMessage message, IBufferWriter output) throws StyxException {
        int packetSize = getMessageSize(message);
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

    private void serializeTMessage(StyxMessage message, IBufferWriter output) throws StyxException {
        if (FID_MESSAGES.contains(message.getType())) {
            output.writeUInt32(((BaseMessage)message).getFID());
        }
        switch (message.getType()) {
            case MessageType.Tversion:
                StyxTVersionMessage tVersionMessage = (StyxTVersionMessage) message;
                output.writeUInt32(tVersionMessage.getIounit());
                output.writeUTFString(tVersionMessage.protocolVersion);
                break;
            case MessageType.Tcreate:
                StyxTCreateMessage tCreate = (StyxTCreateMessage) message;
                output.writeUTFString(tCreate.name);
                output.writeUInt32(tCreate.permissions);
                output.writeUInt8((short) tCreate.mode);
                break;
            case MessageType.Twalk:
                StyxTWalkMessage tWalk = (StyxTWalkMessage) message;
                output.writeUInt32(tWalk.mNewFID);
                if (tWalk.mPathElements != null) {
                    output.writeUInt16(tWalk.mPathElements.size());
                    for (String pathElement : tWalk.mPathElements)
                        output.writeUTFString(pathElement);
                } else {
                    output.writeUInt16(0);
                }
                break;
            case MessageType.Twrite:
                StyxTWriteMessage tWriteMessage = (StyxTWriteMessage) message;
                output.writeUInt64(tWriteMessage.offset);
                output.writeUInt32(tWriteMessage.dataLength);
                output.write(tWriteMessage.data, tWriteMessage.dataOffset, tWriteMessage.dataLength);
                break;
            case MessageType.Tauth:
                StyxTAuthMessage tAuthMessage = (StyxTAuthMessage) message;
                output.writeUTFString(tAuthMessage.mUserName);
                output.writeUTFString(tAuthMessage.mMountPoint);
                break;
            case MessageType.Tread:
                StyxTReadMessage tReadMessage = (StyxTReadMessage) message;
                output.writeUInt64(tReadMessage.offset);
                output.writeUInt32(tReadMessage.count);
                break;
            case MessageType.Twstat:
                StyxTWStatMessage twStatMessage = (StyxTWStatMessage) message;
                // TODO something wrong with size
                output.writeUInt16(getStatSerializedSize(twStatMessage.stat));
                serializeStat(twStatMessage.stat, output);
                break;
            case MessageType.Tflush:
                StyxTFlushMessage tFlushMessage = (StyxTFlushMessage) message;
                output.writeUInt16(tFlushMessage.oldTag);
                break;
            case MessageType.Topen:
                StyxTOpenMessage tOpenMessage = (StyxTOpenMessage) message;
                output.writeUInt8((short) tOpenMessage.mode);
                break;
            case MessageType.Tattach:
                StyxTAttachMessage tAttachMessage = (StyxTAttachMessage) message;
                output.writeUInt32(tAttachMessage.authFID);
                output.writeUTFString(tAttachMessage.userName);
                output.writeUTFString(tAttachMessage.mountPoint);
                break;
        }
    }

    private void serializeRMessage(StyxMessage message, IBufferWriter output) throws StyxException {
        if (message instanceof BaseMessage && ((BaseMessage) message).mQID != null) {
            serializeQid(((BaseMessage) message).mQID, output);
        }
        switch (message.getType()) {
            case MessageType.Rerror:
                output.writeUTFString(((StyxRErrorMessage)message).mError);
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
                output.writeUInt32(read.dataLength);
                if ( read.dataLength > 0 ) {
                    output.write(read.data, 0, read.dataLength);
                }
                break;
            case MessageType.Rstat:
                StyxRStatMessage rStatMessage = (StyxRStatMessage) message;
                output.writeUInt16(getStatSerializedSize(rStatMessage.stat));
                serializeStat(rStatMessage.stat, output);
                break;
            case MessageType.Rcreate:
            case MessageType.Ropen:
                BaseMessage rOpenMessage = (BaseMessage) message;
                output.writeUInt32(rOpenMessage.getIounit());
                break;
            case MessageType.Rwalk:
                StyxRWalkMessage rWalkMessage = (StyxRWalkMessage) message;
                output.writeUInt16(rWalkMessage.qidList.size());
                for (var qid : rWalkMessage.qidList)
                    serializeQid(qid, output);
                break;
            case MessageType.Rclunk:
                output.writeUInt32(((BaseMessage)message).getFID());
                break;
        }
    }

    private long DateToInt(Date date) {
        if (date == null)
            return 0;
        return date.getTime() / 1000;
    }

    @Override
    public void serializeStat(StyxStat stat, IBufferWriter output)
            throws StyxException {
        int size = getStatSerializedSize(stat);
        output.writeUInt16(size - 2); // total size except first 2 bytes with size
        output.writeUInt16(stat.type());
        output.writeUInt32(stat.dev());
        serializeQid(stat.QID(), output);
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
    public void serializeQid(StyxQID qid, IBufferWriter output) throws StyxException {
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
