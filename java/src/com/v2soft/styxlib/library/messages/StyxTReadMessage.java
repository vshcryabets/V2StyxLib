package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ULong;

public class StyxTReadMessage extends StyxTMessage {
	private long mFID;
	private ULong mOffset;
	private long mCount;

	public StyxTReadMessage(long fid, ULong offset, long count)	{
		super(MessageType.Tread);
		mFID = fid;
		mOffset = offset;
		mCount = count;
	}
	
    @Override
    public void load(IStyxDataReader input) 
        throws IOException  {
        setFID(input.readUInt32());
        setOffset(input.readUInt64());
        setCount(input.readUInt32());
    }
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	public ULong getOffset()
	{
		return mOffset;
	}
	
	public void setOffset(ULong offset)
	{
		mOffset = offset;
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
		return super.getBinarySize() + 16;
	}
	
	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
        output.writeUInt32(getFID());
        output.writeUInt64(getOffset());
        output.writeUInt32(getCount());
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nOffset: %s\nCount: %d",
				getFID(), getOffset().toString(), getCount());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rread;
	}
	
}
