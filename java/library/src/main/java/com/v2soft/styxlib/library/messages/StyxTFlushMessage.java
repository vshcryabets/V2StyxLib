package com.v2soft.styxlib.library.messages;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTFlushMessage extends StyxTMessage {
	private int mOldTag;

	public StyxTFlushMessage(int tag){
		super(MessageType.Tflush, MessageType.Rflush);
		mOldTag = tag;
	}

    @Override
    public void load(IStyxDataReader input) 
        throws IOException  {
        super.load(input);
        mOldTag = input.readUInt16();
    }

	public int getOldTag(){return mOldTag;}
	public void setOldTag(int oldTag){mOldTag = oldTag;}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 2;
	}
	
	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
	    output.writeUInt16(mOldTag);
	}

	@Override
    public String toString() {
		return String.format("%s\nTag: %d", super.toString(), getOldTag());
	}
}
