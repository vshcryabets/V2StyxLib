package com.v2soft.styxandroid.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;
import com.v2soft.styxandroid.library.messages.base.structs.StyxQID;

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
	protected void internalWriteToStream(StyxOutputStream output)
		throws IOException
	{
		getQID().writeBinaryTo(output);		
	}

	@Override
	protected String internalToString() {
		return String.format("QID: %s", getQID().toString());
	}
	
}
