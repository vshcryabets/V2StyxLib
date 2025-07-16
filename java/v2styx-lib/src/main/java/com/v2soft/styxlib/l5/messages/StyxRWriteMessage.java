package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;

public class StyxRWriteMessage extends BaseMessage {
	public final long count;

	public StyxRWriteMessage(int tag, long count) {
		super(MessageType.Rwrite, tag, null);
		this.count = count;
	}

	@Override
    public String toString() {
		return String.format("%s\nCount: %d", super.toString(), count);
	}

}
