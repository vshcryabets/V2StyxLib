package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTRemoveMessage extends StyxTMessage {
	private long mFID;

	public StyxTRemoveMessage()
	{
		this(NOFID);
	}
	
	public StyxTRemoveMessage(long fid)
	{
		super(MessageType.Tremove);
		mFID = fid;
	}
	
	public StyxTRemoveMessage(int tag)
	{
		this(tag, NOFID);
	}
	
	public StyxTRemoveMessage(int tag, long fid)
	{
		super(MessageType.Tremove, tag);
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
		return MessageType.Rremove;
	}
	
}
