package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

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
    public void load(StyxInputStream input) 
        throws IOException  {
    }
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize();
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		
	}

	@Override
	protected String internalToString() {
		return null;
	}
	
}
