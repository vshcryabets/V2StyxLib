package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxRWriteMessage extends BaseMessage {
	public final long count;

	protected StyxRWriteMessage(int tag, long count) {
		super(MessageType.Rwrite, tag, null, 0, 0);
		this.count = count;
	}

	@Override
    public String toString() {
		return String.format("%s\nCount: %d", super.toString(), count);
	}

}
