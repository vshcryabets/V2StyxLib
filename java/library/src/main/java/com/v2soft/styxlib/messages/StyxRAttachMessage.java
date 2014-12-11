package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.messages.base.structs.StyxQID;

public class StyxRAttachMessage extends StyxRSingleQIDMessage {
	public StyxRAttachMessage(int tag, StyxQID qid) {
		super(MessageType.Rattach, tag, qid);
	}
}
