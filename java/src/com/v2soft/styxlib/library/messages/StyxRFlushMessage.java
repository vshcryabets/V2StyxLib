package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRFlushMessage extends StyxMessage {

	public StyxRFlushMessage()
	{
		super(MessageType.Rflush);
	}
	
	public StyxRFlushMessage(int tag)
	{
		super(MessageType.Rflush, tag);
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
