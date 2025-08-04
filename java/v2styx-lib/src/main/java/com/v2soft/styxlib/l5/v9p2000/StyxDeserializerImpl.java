package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.StyxRReadMessage;
import com.v2soft.styxlib.l5.messages.StyxRStatMessage;
import com.v2soft.styxlib.l5.messages.StyxRWalkMessage;
import com.v2soft.styxlib.l5.messages.StyxRWriteMessage;
import com.v2soft.styxlib.l5.messages.StyxTCreateMessage;
import com.v2soft.styxlib.l5.messages.StyxTFlushMessage;
import com.v2soft.styxlib.l5.messages.StyxTOpenMessage;
import com.v2soft.styxlib.l5.messages.StyxTReadMessage;
import com.v2soft.styxlib.l5.messages.base.Factory;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.util.Date;
import java.util.LinkedList;

public class StyxDeserializerImpl implements IDataDeserializer {
    public final Factory messageFactory;

    public StyxDeserializerImpl(Factory messageFactory) {
        this.messageFactory = messageFactory;
    }

    @Override
    public StyxMessage deserializeMessage(IBufferReader buffer, int io_unit) throws StyxException {
        // get common packet data
        long packet_size = buffer.readUInt32();
        if (packet_size > io_unit) {
            throw new StyxException("Packet size to large");
        }
        var typeId = buffer.readUInt8();
        int tag = buffer.readUInt16();
        // load other data
        StyxMessage result = null;
        switch (typeId) {
            case MessageType.Tversion -> result = messageFactory.constructTVersion(buffer.readUInt32(), buffer.readUTFString());
            case MessageType.Rversion -> result = messageFactory.constructRVersion(buffer.readUInt32(), buffer.readUTFString());
            case MessageType.Tauth -> result = messageFactory.constructTAuth(
                    buffer.readUInt32(),
                    buffer.readUTFString(),
                    buffer.readUTFString());
            case MessageType.Tflush -> result = new StyxTFlushMessage(buffer.readUInt16());
            case MessageType.Tattach -> result = messageFactory.constructTAttach(buffer.readUInt32(),
                    buffer.readUInt32(),
                    buffer.readUTFString(),
                    buffer.readUTFString());
            case MessageType.Twalk -> {
                var fid = buffer.readUInt32();
                var newFid = buffer.readUInt32();
                var count = buffer.readUInt16();
                var pathElements = new LinkedList<String>();
                for (int i = 0; i < count; i++) {
                    pathElements.add(buffer.readUTFString());
                }
                result = messageFactory.constructTWalkMessage(fid,
                        newFid,
                        pathElements);
            }
            case MessageType.Rauth -> result = messageFactory.constructRAuthMessage(tag, deserializeQid(buffer));
            case MessageType.Rerror -> result = messageFactory.constructRerror(tag, buffer.readUTFString());
            case MessageType.Rflush -> result = new BaseMessage(MessageType.Rflush, tag, null);
            case MessageType.Rattach -> result = messageFactory.constructRAttachMessage(tag, deserializeQid(buffer));
            case MessageType.Rwalk -> {
                var count = buffer.readUInt16();
                var qids = new LinkedList<StyxQID>();
                for (int i = 0; i < count; i++) {
                    qids.add(deserializeQid(buffer));
                }
                result = new StyxRWalkMessage(tag, qids);
            }
            case MessageType.Topen -> result = new StyxTOpenMessage(buffer.readUInt32(),
                    buffer.readUInt8());
            case MessageType.Ropen -> result = messageFactory.constructROpenMessage(tag,
                    deserializeQid(buffer),
                    buffer.readUInt32());
            case MessageType.Tcreate -> result = new StyxTCreateMessage(buffer.readUInt32(),
                    buffer.readUTFString(),
                    buffer.readUInt32(),
                    buffer.readUInt8());
            case MessageType.Rcreate -> result = messageFactory.constructRCreateMessage(
                    tag,
                    deserializeQid(buffer),
                    buffer.readUInt32());
            case MessageType.Tread -> result = new StyxTReadMessage(
                    buffer.readUInt32(),
                    buffer.readUInt64(),
                    (int) buffer.readUInt32());
            case MessageType.Rread -> {
                var dataLength = (int) buffer.readUInt32();
                MetricsAndStats.byteArrayAllocationRRead++;
                var data = new byte[dataLength];
                buffer.readData(data, 0, dataLength);
                result = new StyxRReadMessage(tag,
                        data,
                        dataLength);
            }
            case MessageType.Twrite -> {
                var fid = buffer.readUInt32();
                var offset = buffer.readUInt64();
                var dataLength = (int) buffer.readUInt32();
                var data = new byte[dataLength];
                MetricsAndStats.byteArrayAllocationTWrite++;
                buffer.readData(data, 0, dataLength);
                result = messageFactory.constructTWriteMessage(fid,
                        offset,
                        data,
                        0,
                        0);
            }
            case MessageType.Rwrite -> result = new StyxRWriteMessage(tag, buffer.readUInt32());
            case MessageType.Tclunk -> result = new StyxTMessageFID(
                    MessageType.Tclunk,
                    buffer.readUInt32());
            case MessageType.Rclunk -> result = new BaseMessage(MessageType.Rclunk, tag, null);
            case MessageType.Tremove -> result = new StyxTMessageFID(MessageType.Tremove,
                    buffer.readUInt32());
            case MessageType.Rremove -> result = new BaseMessage(MessageType.Rremove, tag, null);
            case MessageType.Tstat -> result = new StyxTMessageFID(MessageType.Tstat,
                    buffer.readUInt32());
            case MessageType.Rstat -> {
                buffer.readUInt16(); //??
                result = new StyxRStatMessage(tag, deserializeStat(buffer));
            }
            case MessageType.Twstat -> {
                var fid = buffer.readUInt32();
                buffer.readUInt16(); // ???
                var stat = deserializeStat(buffer);
                result = messageFactory.constructTWStatMessage(fid, stat);
            }
            case MessageType.Rwstat -> result = new BaseMessage(MessageType.Rwstat, tag, null);
            default ->
                throw new StyxException("Type is null, can't decode message ps=" +
                        packet_size + " typeId=" + typeId);
        }
        result.setTag((short) tag);
        return result;
    }

    public static Date intToDate(long date) {
        return new Date(date * 1000L);
    }

    @Override
    public StyxStat deserializeStat(IBufferReader input) throws StyxException {
        int size = input.readUInt16(); // skip size bytes
        // TODO check size
        short type = (short) input.readUInt16();
        var dev = (int) input.readUInt32();
        var qid = deserializeQid(input);
        var mode = (int) input.readUInt32();
        var accessTime = intToDate(input.readUInt32());
        var modificationTime = intToDate(input.readUInt32());
        var length = input.readUInt64();
        var name = input.readUTFString();
        var userName = input.readUTFString();
        var groupName = input.readUTFString();
        var modificationUser = input.readUTFString();
        return new StyxStat(
                type,
                dev,
                qid,
                mode,
                accessTime,
                modificationTime,
                length,
                name,
                userName,
                groupName,
                modificationUser
        );
    }

    @Override
    public StyxQID deserializeQid(IBufferReader input) throws StyxException {
        return new StyxQID(
                input.readUInt8(),
                input.readUInt32(),
                input.readUInt64()
        );
    }
}
