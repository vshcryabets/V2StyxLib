package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRAttachMessage extends StyxMessage {
	private StyxQID mQID;

	public StyxRAttachMessage()
	{
		super(MessageType.Rattach);
	}
	
	public StyxRAttachMessage(int tag)
	{
		super(MessageType.Rattach, tag);
	}
	
	public StyxRAttachMessage(int tag, StyxQID qid)
	{
		super(MessageType.Rattach, tag);
		setQID(qid);
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
    }
	
	public StyxQID getQID()
	{
		if (mQID == null)
			return StyxQID.EMPTY;
		return mQID;
	}
	
	public void setQID(StyxQID qid)
	{
		mQID = qid;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + StyxQID.CONTENT_SIZE;
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		getQID().writeBinaryTo(output);		
	}

	@Override
	protected String internalToString() {
		return String.format("QID: %s", getQID().toString());
	}

    @Override
    protected void load(DualStateBuffer buffer) throws IOException {
    }
	
}
