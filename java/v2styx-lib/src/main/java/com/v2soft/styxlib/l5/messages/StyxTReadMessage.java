package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTReadMessage extends StyxTMessageFID {
	public final long offset;
	public final int count;

	public StyxTReadMessage(long fid, long offset, int count)	{
		super(MessageType.Tread, fid);
		this.offset = offset;
		this.count = count;
	}

	@Override
    public String toString() {
	    return String.format("%s\nOffset: %d\nCount: %d",
				super.toString(), offset, count);
	}
}
