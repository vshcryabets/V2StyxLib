package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRWriteMessage extends StyxMessage {
	private long mCount;

	public StyxRWriteMessage(int tag, long count) {
		super(MessageType.Rwrite, tag);
		mCount = count;
	}
	
    @Override
    public void load(IStyxDataReader input) 
        throws IOException  {
        mCount = input.readUInt32();
    }
	
	public long getCount()
	{
		return mCount;
	}
		
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4;
	}
	
	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUInt32(mCount);
	}

	@Override
    public String toString() {
		return String.format("%s\nCount: %d", super.toString(), mCount);
	}
	
}
