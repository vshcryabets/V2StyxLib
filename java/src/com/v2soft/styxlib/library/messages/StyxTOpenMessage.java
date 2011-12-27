package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;

public class StyxTOpenMessage extends StyxTMessage {
	private long mFID;
	private ModeType mMode;

	public StyxTOpenMessage()
	{
		this(NOFID, ModeType.OREAD);
	}
	
	public StyxTOpenMessage(long fid, ModeType mode)
	{
		super(MessageType.Topen);
		mFID = fid;
		mMode = mode;
	}
	
	public StyxTOpenMessage(int tag)
	{
		this(tag, NOFID, ModeType.OREAD);
	}
	
	public StyxTOpenMessage(int tag, long fid, ModeType mode) {
		super(MessageType.Topen, tag);
		mFID = fid;
		mMode = mode;
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setFID(input.readUInt());
		setMode(ModeType.factory(input.readUByte()));
	}
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	public ModeType getMode()
	{
		return mMode;
	}
	
	public void setMode(ModeType mode)
	{
		mMode = mode;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 5;
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getFID());
		output.writeUByte(getMode().getByte());		
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nMode: %s", 
				getFID(), getMode().toString());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Ropen;
	}
	
}
