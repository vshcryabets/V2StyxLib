package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;

import java.io.IOException;

public class StyxTFlushMessage extends StyxTMessage {
	private int mOldTag;

	public StyxTFlushMessage(int tag){
		super(MessageType.Tflush, MessageType.Rflush);
		mOldTag = tag;
	}

    @Override
    public void load(IBufferReader input)
        throws IOException  {
        super.load(input);
        mOldTag = input.readUInt16();
    }

	public int getOldTag(){return mOldTag;}

	@Override
    public String toString() {
		return String.format("%s\nTag: %d", super.toString(), getOldTag());
	}
}
