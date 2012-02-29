package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;

public class StyxRRemoveMessage extends StyxMessage {

	public StyxRRemoveMessage()
	{
		super(MessageType.Rremove);
	}
	
	public StyxRRemoveMessage(int tag)
	{
		super(MessageType.Rremove, tag);
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
	protected String internalToString() {
		return null;
	}
	
}
