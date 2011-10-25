package com.v2soft.styxandroid.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;

public class StyxRWriteMessage extends StyxMessage {
	private long mCount;

	public StyxRWriteMessage()
	{
		this(0);
	}
	
	public StyxRWriteMessage(long count)
	{
		super(MessageType.Rwrite);
		mCount = count;
	}
	
	public StyxRWriteMessage(int tag)
	{
		this(tag, 0);
	}
	
	public StyxRWriteMessage(int tag, long count)
	{
		super(MessageType.Rwrite, tag);
		mCount = count;
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setCount(input.readUInt());
	}
	
	public long getCount()
	{
		return mCount;
	}
	
	public void setCount(long count)
	{
		mCount = count;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4;
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getCount());
	}

	@Override
	protected String internalToString() {
		return String.format("Count: %d", getCount());
	}
	
}
