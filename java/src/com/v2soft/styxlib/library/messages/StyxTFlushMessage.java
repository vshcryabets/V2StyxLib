package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxTFlushMessage extends StyxTMessage {
	private int mOldTag;

	public StyxTFlushMessage()
	{
		super(MessageType.Tflush);
	}
	
	public StyxTFlushMessage(int tag)
	{
		super(MessageType.Tflush, tag);
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setOldTag(input.readUInt16());
	}
    @Override
    public void load(DualStateBuffer input) 
        throws IOException  {
        setOldTag(input.readUInt16());
    }

	public int getOldTag()
	{
		return mOldTag;
	}
	
	public void setOldTag(int oldTag)
	{
		mOldTag = oldTag;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 2;
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
	    output.writeUShort(getOldTag());
	}

	@Override
	protected String internalToString() {
		return String.format("OldTag: %d", getOldTag());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rflush;
	}
}
