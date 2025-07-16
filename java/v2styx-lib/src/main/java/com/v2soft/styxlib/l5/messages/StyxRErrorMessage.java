package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;

public class StyxRErrorMessage extends BaseMessage {
	public final String mError;

	public StyxRErrorMessage(int tag, String error) {
		super(MessageType.Rerror, tag, null);
		mError = error;
	}

	@Override
	public String toString() {
	    return String.format("%s\nError: %s", super.toString(), mError);
	}
}
