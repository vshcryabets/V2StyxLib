package com.v2soft.styxandroid.library.messages;

import java.io.IOException;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;
import com.v2soft.styxandroid.library.messages.base.structs.StyxQID;

public class StyxROpenMessage extends StyxMessage {
	private StyxQID mQID;
	private long mIOUnit;

	public StyxROpenMessage(int tag)
	{
		super(MessageType.Ropen, tag);
	}
/*	
	public StyxROpenMessage(StyxQID qid, long iounit)
	{
		super(MessageType.Ropen);
		mQID = qid;
		mIOUnit = iounit;
	}
	
	public StyxROpenMessage(int tag, StyxQID qid)
	{
		this(tag, qid, 0);
	}
	
	public StyxROpenMessage(int tag, StyxQID qid, int iounit)
	{
		super(MessageType.Ropen, tag);
		mQID = qid;
		mIOUnit = iounit;
	}*/
	
	@Override
	public void load(StyxInputStream stream) throws IOException
	{
	    mQID = new StyxQID(stream);
	    mIOUnit = stream.readUInt();
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
	
	public long getIOUnit()
	{
		return mIOUnit;
	}
	
	public void setIOUnit(long iounit)
	{
		mIOUnit = iounit;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize()
			+ StyxQID.CONTENT_SIZE + 4;
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		getQID().writeBinaryTo(output);
		output.writeUInt(getIOUnit());
	}

	@Override
	protected String internalToString() {
		return String.format("QID: %s\nIOUnit: %d", 
				getQID().toString(), getIOUnit());
	}
	
}
