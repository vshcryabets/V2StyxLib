package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;

public class StyxRWriteMessage extends StyxMessage {
	public final long count;

	public StyxRWriteMessage(int tag, long count) {
		super(MessageType.Rwrite, tag);
		this.count = count;
	}

	@Override
    public String toString() {
		return String.format("%s\nCount: %d", super.toString(), count);
	}

}
