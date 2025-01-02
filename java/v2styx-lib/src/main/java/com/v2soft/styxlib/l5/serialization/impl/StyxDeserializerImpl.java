package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

public class StyxDeserializerImpl implements IDataDeserializer {
    @Override
    public StyxMessage deserializeMessage(IBufferReader buffer, int io_unit) throws StyxException {
        // get common packet data
        long packet_size = buffer.readUInt32();
        if (packet_size > io_unit) {
            throw new StyxException("Packet size to large");
        }
        var typeId = buffer.readUInt8();
        MessageType type = MessageType.factory(typeId);
        if (type == null) {
            throw new NullPointerException("Type is null, can't decode message ps=" + packet_size + " typeId=" + typeId);
        }
        int tag = buffer.readUInt16();
        // load other data
        StyxMessage result = null;
        switch (type) {
            case Tversion -> result = new StyxTVersionMessage(buffer.readUInt32(), buffer.readUTFString());
            case Rversion -> result = new StyxRVersionMessage(buffer.readUInt32(), buffer.readUTFString());
            case Tauth -> result = new StyxTAuthMessage(
                    buffer.readUInt32(),
                    buffer.readUTFString(),
                    buffer.readUTFString());
            case Tflush -> result = new StyxTFlushMessage(buffer.readUInt16());
            case Tattach -> result = new StyxTAttachMessage(buffer.readUInt32(),
                    buffer.readUInt32(),
                    buffer.readUTFString(),
                    buffer.readUTFString());
            case Twalk -> {
                var fid = buffer.readUInt32();
                var newFid = buffer.readUInt32();
                var count = buffer.readUInt16();
                var pathElements = new LinkedList<String>();
                for (int i = 0; i < count; i++) {
                    pathElements.add(buffer.readUTFString());
                }
                result = new StyxTWalkMessage(fid,
                        newFid,
                        pathElements);
            }
            case Rauth -> result = new StyxRAuthMessage(tag, deserializeQid(buffer));
            case Rerror -> result = new StyxRErrorMessage(tag, buffer.readUTFString());
            case Rflush -> result = new StyxMessage(MessageType.Rflush, tag);
            case Rattach -> result = new StyxRAttachMessage(tag, deserializeQid(buffer));
            case Rwalk -> {
                var count = buffer.readUInt16();
                var qids = new LinkedList<StyxQID>();
                for (int i = 0; i < count; i++) {
                    qids.add(deserializeQid(buffer));
                }
                result = new StyxRWalkMessage(tag, qids);
            }
            case Topen -> result = new StyxTOpenMessage(buffer.readUInt32(),
                    buffer.readUInt8());
            case Ropen -> result = new StyxROpenMessage(tag,
                    deserializeQid(buffer),
                    buffer.readUInt32(), false);
            case Tcreate -> result = new StyxTCreateMessage(buffer.readUInt32(),
                    buffer.readUTFString(),
                    buffer.readUInt32(),
                    buffer.readUInt8());
            case Rcreate -> result = new StyxROpenMessage(
                    tag,
                    deserializeQid(buffer),
                    buffer.readUInt32(), true);
            case Tread -> result = new StyxTReadMessage(
                    buffer.readUInt32(),
                    buffer.readUInt64(),
                    buffer.readUInt32());
            case Rread -> {
                var dataLength = (int) buffer.readUInt32();
                MetricsAndStats.byteArrayAllocationRRead++;
                var data = new byte[dataLength];
                buffer.readData(data, 0, dataLength);
                result = new StyxRReadMessage(tag,
                        data,
                        dataLength);
            }
            case Twrite -> {
                var fid = buffer.readUInt32();
                var offset = buffer.readUInt64();
                var dataLength = (int) buffer.readUInt32();
                var data = new byte[dataLength];
                MetricsAndStats.byteArrayAllocationTWrite++;
                buffer.readData(data, 0, dataLength);
                result = new StyxTWriteMessage(fid,
                        offset,
                        data,
                        0,
                        0);
            }
            case Rwrite -> result = new StyxRWriteMessage(tag, buffer.readUInt32());
            case Tclunk -> result = new StyxTMessageFID(
                    MessageType.Tclunk,
                    MessageType.Rclunk,
                    buffer.readUInt32());
            case Rclunk -> result = new StyxMessage(MessageType.Rclunk, tag);
            case Tremove -> result = new StyxTMessageFID(MessageType.Tremove, MessageType.Rremove,
                    buffer.readUInt32());
            case Rremove -> result = new StyxMessage(MessageType.Rremove, tag);
            case Tstat -> result = new StyxTMessageFID(MessageType.Tstat, MessageType.Rstat,
                    buffer.readUInt32());
            case Rstat -> {
                buffer.readUInt16(); //??
                result = new StyxRStatMessage(tag, deserializeStat(buffer));
            }
            case Twstat -> {
                var fid = buffer.readUInt32();
                buffer.readUInt16(); // ???
                var stat = deserializeStat(buffer);
                result = new StyxTWStatMessage(fid, stat);
            }
            case Rwstat -> result = new StyxMessage(MessageType.Rwstat, tag);
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
