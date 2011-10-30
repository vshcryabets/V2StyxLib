package com.v2soft.styxlib.library.exceptions;

import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxErrorMessageException extends StyxException {

	private static final long serialVersionUID = 5740549304501726254L;
	
	public static void doException(StyxMessage rMessage) 
		throws StyxErrorMessageException
	{
		if (rMessage == null)
			throw new NullPointerException();
		if (rMessage.getType() != MessageType.Rerror)
			return;
		
		StyxRErrorMessage rError = (StyxRErrorMessage) rMessage;
		throw new StyxErrorMessageException(rError);
	}
	
	private StyxRErrorMessage mMessage;
	
	private StyxErrorMessageException(StyxRErrorMessage message)
	{
		super(message.getError());
		mMessage = message;
	}
	
	public StyxRErrorMessage getErrorMessage()
	{
		return mMessage;
	}

}
