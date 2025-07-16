package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.dev.StringSerializer;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StringSerializerImpl implements StringSerializer {
    @Override
    public String serializeQid(StyxQID qid) throws StyxException {
        return String.format("QID {type: %d, version: %d, path: %d}",
                qid.type(), qid.version(), qid.path());
    }

    @Override
    public String serializeStat(StyxStat stat) throws StyxException {
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
        return "";
    }
}
