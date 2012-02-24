package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;

public class StyxRClunkMessage extends StyxMessage {
	
	public StyxRClunkMessage()
	{
		super(MessageType.Rclunk);
	}
	
	public StyxRClunkMessage(int tag)
	{
		super(MessageType.Rclunk, tag);
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
    }
    @Override
    public void load(DualStateBuffer input) 
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
