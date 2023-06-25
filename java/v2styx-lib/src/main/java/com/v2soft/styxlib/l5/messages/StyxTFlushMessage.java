package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.serialization.IStyxDataReader;
import com.v2soft.styxlib.l5.serialization.BufferWritter;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.enums.MessageType;

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
    public String toString() {
		return String.format("%s\nTag: %d", super.toString(), getOldTag());
	}
}
