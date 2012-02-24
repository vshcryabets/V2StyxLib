package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.types.ULong;

public class StyxTReadMessage extends StyxTMessage {
	private long mFID;
	private ULong mOffset;
	private long mCount;

	public StyxTReadMessage()
	{
		this(NOFID, ULong.ZERO, 0);
	}
	
	public StyxTReadMessage(long fid, ULong offset, long count)
	{
		super(MessageType.Tread);
		mFID = fid;
		mOffset = offset;
		mCount = count;
	}
	
	public StyxTReadMessage(int tag)
	{
		this(tag, NOFID, ULong.ZERO, 0);
	}
	
	public StyxTReadMessage(int tag, long fid, ULong offset, long count)
	{
		super(MessageType.Tread, tag);
		mFID = fid;
		mOffset = offset;
		mCount = count;
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setFID(input.readUInt32());
		setOffset(input.readUInt64());
		setCount(input.readUInt32());
	}
    @Override
    public void load(DualStateBuffer input) 
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
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException {
		output.writeUInt(getFID());
		output.writeULong(getOffset());
		output.writeUInt(getCount());
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nOffset: %s\nCount: %d",
				getFID(), getOffset().toString(), getCount());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rread;
	}
	
}
