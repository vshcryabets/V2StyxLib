package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;

import java.io.IOException;

public class StyxTReadMessage extends StyxTMessageFID {
	private final long mOffset;
	private final int mCount;

	public StyxTReadMessage(long fid, long offset, int count)	{
		super(MessageType.Tread, MessageType.Rread, fid);
		mOffset = offset;
		mCount = count;
	}

	public long getOffset()
	{
		return mOffset;
	}

	public int getCount()
	{
		return mCount;
	}

	@Override
    public String toString() {
	    return String.format("%s\nOffset: %d\nCount: %d",
				super.toString(), getOffset(), getCount());
	}
}
