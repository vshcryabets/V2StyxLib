package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.structs.QID;

public class StyxTMessage extends BaseMessage {
    protected StyxTMessage(int type,
                           QID qid,
                           long fid,
                           long iounit,
                           String protocolVersion) {
        super(type, Constants.NOTAG, qid, fid, iounit, protocolVersion);
    }
}
