package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;

public class StyxTMessage extends BaseMessage {
    public StyxTMessage(int type) {
        super(type, Constants.NOTAG, null);
    }
}
