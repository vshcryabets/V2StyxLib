package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;

import java.io.IOException;

public class StyxTReadMessage extends StyxTMessageFID {
	private long mOffset;
	private long mCount;

	public StyxTReadMessage(long fid, long offset, long count)	{
		super(MessageType.Tread, MessageType.Rread, fid);
		mOffset = offset;
		mCount = count;
	}

    @Override
    public void load(IStyxDataReader input)
        throws IOException  {
        super.load(input);
        mOffset = input.readUInt64();
        mCount = input.readUInt32();
    }

	public long getOffset()
	{
		return mOffset;
	}

	public void setOffset(long offset)
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
		return super.getBinarySize() + 8 + 4;
	}

	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws IOException {
	    super.writeToBuffer(output);
        output.writeUInt64(mOffset);
        output.writeUInt32(mCount);
	}

	@Override
    public String toString() {
	    return String.format("%s\nOffset: %d\nCount: %d",
				super.toString(), getOffset(), getCount());
	}
}
