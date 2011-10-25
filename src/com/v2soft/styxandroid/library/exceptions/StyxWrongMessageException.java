package com.v2soft.styxandroid.library.exceptions;

import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;


public class StyxWrongMessageException extends StyxException {

	private static final long serialVersionUID = 9071135844358484201L;
	
	public StyxWrongMessageException(StyxMessage received, MessageType needed)
	{
		super(String.format("Recived massage of type %s when needed %s.", 
				received.getType().toString(), needed.toString()));
	}

}
