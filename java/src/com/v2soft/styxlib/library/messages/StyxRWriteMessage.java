package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRWriteMessage extends StyxMessage {
	private long mCount;

	public StyxRWriteMessage(int tag, long count) {
		super(MessageType.Rwrite, tag);
		mCount = count;
	}
	
    @Override
    public void load(StyxDataReader input) 
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
	public void writeToBuffer(StyxDataReader output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUInt(getCount());
	}

	@Override
	protected String internalToString() {
		return String.format("Count: %d", getCount());
	}
	
}
