package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.QID;

import java.util.List;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxTWalkMessage
extends StyxTMessage {
    public final long mNewFID;
    public final List<String> mPathElements;

    protected StyxTWalkMessage(long fid, long new_fid, List<String> path){
        super(MessageType.Twalk, QID.EMPTY, fid, 0, null);
        mNewFID = new_fid;
        mPathElements = path;
    }
}
