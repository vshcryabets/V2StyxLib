package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxTRemoveMessage extends StyxTMessage {
	private long mFID;

	public StyxTRemoveMessage(long fid) {
		super(MessageType.Tremove);
		mFID = fid;
	}
	
    @Override
    public void load(StyxBufferOperations input) 
        throws IOException  {
        setFID(input.readUInt32());
    }
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4;
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
	    output.writeUInt(getFID());
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d", getFID());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rremove;
	}
	
}
