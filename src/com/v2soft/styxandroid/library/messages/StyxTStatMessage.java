package com.v2soft.styxandroid.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxTMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;

public class StyxTStatMessage extends StyxTMessage {
	private long mFID;

	public StyxTStatMessage()
	{
		this(NOFID);
	}
	
	public StyxTStatMessage(long fid)
	{
		super(MessageType.Tstat);
		mFID = fid;
	}
	
	public StyxTStatMessage(int tag)
	{
		this(tag, NOFID);
	}
	
	public StyxTStatMessage(int tag, long fid)
	{
		super(MessageType.Tstat, tag);
		mFID = fid;
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setFID(input.readUInt());
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
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getFID());		
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d", getFID());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rstat;
	}
	
}
