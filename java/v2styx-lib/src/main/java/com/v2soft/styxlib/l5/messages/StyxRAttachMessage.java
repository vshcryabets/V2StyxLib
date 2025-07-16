package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class StyxRAttachMessage extends BaseMessage {
	public StyxRAttachMessage(int tag, StyxQID qid) {
		super(MessageType.Rattach, tag, qid);
	}
}
