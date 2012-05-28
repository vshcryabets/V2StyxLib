package com.v2soft.styxlib.library.messages;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRWStatMessage extends StyxMessage {

	public StyxRWStatMessage(int tag) {
		super(MessageType.Rwstat, tag);
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize();
	}

	@Override
	protected String internalToString() {
		return null;
	}
	
}
