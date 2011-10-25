package com.v2soft.styxandroid.library.core;

import com.v2soft.styxandroid.library.exceptions.StyxException;
import com.v2soft.styxandroid.library.messages.StyxRErrorMessage;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;

public interface MessageReceivedListener {

	void onReceived(StyxMessage tMessage, StyxMessage rMessage)
		throws StyxException;
	
	void onError(StyxMessage tMessage, StyxRErrorMessage error);
	
}
