package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.BufferReader;

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
    public void load(BufferReader input)
        throws IOException  {
        super.load(input);
        mOffset = input.readUInt64();
        mCount = input.readUInt32();
    }

	public long getOffset()
	{
		return mOffset;
	}

	public long getCount()
	{
		return mCount;
	}

	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 8 + 4;
	}

	@Override
    public String toString() {
	    return String.format("%s\nOffset: %d\nCount: %d",
				super.toString(), getOffset(), getCount());
	}
}
