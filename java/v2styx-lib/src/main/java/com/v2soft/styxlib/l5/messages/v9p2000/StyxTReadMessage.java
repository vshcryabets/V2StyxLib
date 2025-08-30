package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxTReadMessage extends StyxTMessage {
	public final long offset;
	public final int count;

	protected StyxTReadMessage(long fid, long offset, int count)	{
		super(MessageType.Tread, null, fid, 0);
		this.offset = offset;
		this.count = count;
	}

	@Override
    public String toString() {
	    return String.format("%s\nOffset: %d\nCount: %d",
				super.toString(), offset, count);
	}
}
