package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxRErrorMessage extends BaseMessage {
	public final String mError;

	protected StyxRErrorMessage(int tag, String error) {
		super(MessageType.Rerror, tag, null, 0, 0);
		mError = error;
	}
}
