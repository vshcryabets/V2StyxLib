package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;

public class StyxTFlushMessage extends StyxTMessage {
	public final int oldTag;

	public StyxTFlushMessage(int tag){
		super(MessageType.Tflush);
		oldTag = tag;
	}

	@Override
    public String toString() {
		return String.format("%s\nTag: %d", super.toString(), oldTag);
	}
}
