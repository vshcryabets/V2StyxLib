package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class StyxTMessage extends BaseMessage {
    protected StyxTMessage(int type,
                           StyxQID qid,
                           long fid,
                           long iounit) {
        super(type, Constants.NOTAG, qid, fid, iounit);
    }
}
