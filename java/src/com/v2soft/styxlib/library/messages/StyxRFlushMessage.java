package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRFlushMessage extends StyxMessage {
	
	public StyxRFlushMessage(int tag)
	{
		super(MessageType.Rflush, tag);
	}
	
    @Override
    public void load(IStyxDataReader input) 
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
