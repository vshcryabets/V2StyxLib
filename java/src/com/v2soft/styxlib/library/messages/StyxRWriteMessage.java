package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRWriteMessage extends StyxMessage {
	private long mCount;

	public StyxRWriteMessage() {
		this(0);
	}
	
	public StyxRWriteMessage(long count) {
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
    public void load(StyxBufferOperations input) 
        throws IOException  {
        setCount(input.readUInt32());
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
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUInt(getCount());
	}

	@Override
	protected String internalToString() {
		return String.format("Count: %d", getCount());
	}
	
}
