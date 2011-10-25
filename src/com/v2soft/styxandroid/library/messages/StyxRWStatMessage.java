package com.v2soft.styxandroid.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;

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
