package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTFlushMessage extends StyxTMessage {
	private int mTag;

	public StyxTFlushMessage(int tag){
		super(MessageType.Tflush);
		mTag = tag;
	}

    @Override
    public void load(IStyxDataReader input) 
        throws IOException  {
        mTag = input.readUInt16();
    }

	public int getOldTag(){return mTag;}
	public void setOldTag(int oldTag){mTag = oldTag;}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 2;
	}
	
	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
	    output.writeUInt16(getOldTag());
	}

	@Override
	protected String internalToString() {
		return String.format("Tag: %d", getOldTag());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rflush;
	}
}
