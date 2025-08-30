package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.QID;

public class StyxTFlushMessage extends StyxTMessage {
	public final int oldTag;

	protected StyxTFlushMessage(int tag){
		super(MessageType.Tflush, QID.EMPTY, 0, 0, null);
		oldTag = tag;
	}

	@Override
    public String toString() {
		return String.format("%s\nTag: %d", super.toString(), oldTag);
	}
}
