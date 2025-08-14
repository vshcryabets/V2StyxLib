package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.l5.dev.StringSerializer;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxROpenMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxRVersionMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTVersionMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTWStatMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTWalkMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTWriteMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StringSerializerImpl implements StringSerializer {
    @Override
    public String serializeQid(StyxQID qid) {
        return String.format("QID {type: %d, version: %d, path: %d}",
                qid.type(), qid.version(), qid.path());
    }

    @Override
    public String serializeStat(StyxStat stat) {
        StringBuilder result = new StringBuilder();
        result.append("Stat ")
                .append(String.format("0x%x,0x%x,", stat.type(), stat.dev()))
                .append("Qid=").append(serializeQid(stat.QID()))
                .append(",mode=0x").append(Long.toHexString(stat.mode()))
                .append(",atime=").append(stat.accessTime().toInstant())
                .append(",mtime=").append(stat.modificationTime().toInstant())
                .append(",length=").append(stat.length())
                .append(",name=").append(stat.name())
                .append(",user=").append(stat.userName())
                .append(",group=").append(stat.groupName())
                .append(",modUser=").append(stat.modificationUser());
        return result.toString();
    }

    @Override
    public String serializeMessage(StyxMessage message) {
        StringBuilder result = new StringBuilder();
        result.append("Message Type:");
        result.append(message.getType());
        result.append(",Tag:");
        result.append(message.getTag());

        if (((BaseMessage)message).mQID != null) {
            result.append(",QID=");
            result.append(serializeQid(((BaseMessage)message).mQID));
        }

        switch (message.getType()) {
            case MessageType.Rversion:
                result.append(",MaxPacketSize:");
                result.append(((StyxRVersionMessage) message).maxPacketSize);
                result.append(",ProtocolVersion:");
                result.append(((StyxRVersionMessage) message).protocolVersion);
                break;
            case MessageType.Tversion:
                result.append(",MaxPacketSize:");
                result.append(((StyxTVersionMessage) message).maxPacketSize);
                result.append(",ProtocolVersion:");
                result.append(((StyxTVersionMessage) message).protocolVersion);
                break;
            case MessageType.Twrite:
                result.append(",fileOffset:");
                result.append(((StyxTWriteMessage)message).offset);
                result.append(",dataLength:");
                result.append(((StyxTWriteMessage)message).dataLength);
                break;
            case MessageType.Ropen:
            case MessageType.Rcreate:
                result.append(",iounit:");
                result.append(((StyxROpenMessage)message).ioUnit);
                break;
            case MessageType.Twalk:
                result.append(",fid:");
                result.append(((BaseMessage)message).getFID());
                result.append(",newFid:");
                result.append(((StyxTWalkMessage)message).mNewFID);
                result.append(",pathElements:");
                result.append(((StyxTWalkMessage)message).mPathElements);
                break;
            case MessageType.Twstat:
                result.append(",fid:");
                result.append(((BaseMessage)message).getFID());
                result.append(",Stat:");
                result.append(serializeStat(((StyxTWStatMessage)message).stat));
                break;
            default:
                result.append(" (not implemented for this message type)");
                break;
        }
        return result.toString();
    }
}
