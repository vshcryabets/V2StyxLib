package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRWStatMessage extends StyxMessage {

	public StyxRWStatMessage()
	{
		super(MessageType.Rwstat);
	}
	
	public StyxRWStatMessage(int tag)
	{
		super(MessageType.Rwstat, tag);
	}
	
    @Override
    public void load(StyxBufferOperations input) 
        throws IOException  {
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
