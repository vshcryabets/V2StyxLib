package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxRErrorMessage extends BaseMessage {
	public final String mError;

	public StyxRErrorMessage(int tag, String error) {
		super(MessageType.Rerror, tag, null);
		mError = error;
	}
}
