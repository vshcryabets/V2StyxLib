package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;

public interface MessageReceivedListener {

	void onReceived(StyxMessage tMessage, StyxMessage rMessage)
		throws StyxException;
	
	void onError(StyxMessage tMessage, StyxRErrorMessage error);
	
}
