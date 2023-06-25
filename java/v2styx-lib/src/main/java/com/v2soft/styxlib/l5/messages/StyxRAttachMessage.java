package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class StyxRAttachMessage extends StyxRSingleQIDMessage {
	public StyxRAttachMessage(int tag, StyxQID qid) {
		super(MessageType.Rattach, tag, qid);
	}
}
