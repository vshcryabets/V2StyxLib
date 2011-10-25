package com.v2soft.styxandroid.library.messages;

import java.io.IOException;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxTMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;
import com.v2soft.styxandroid.library.messages.base.structs.StyxStat;

public class StyxTWStatMessage extends StyxTMessage {
	private long mFID;
	private StyxStat mStat;

	public StyxTWStatMessage(StyxStat stat)
	{
		this(NOFID, stat);
	}
	
	public StyxTWStatMessage(long fid, StyxStat stat)
	{
		super(MessageType.Twstat);
		mFID = fid;
		mStat = stat;
	}
	
	public StyxTWStatMessage(int tag, StyxStat stat)
	{
		this(tag, NOFID, stat);
	}
	
	public StyxTWStatMessage(int tag, long fid, StyxStat stat) {
	    this(tag);
		mFID = fid;
		mStat = stat;
	}
	
	public StyxTWStatMessage(int tag) {
	    super(MessageType.Twstat, tag);
    }

	@Override
    public void load(StyxInputStream input) throws IOException {
	    mFID = input.readUInt();
		input.readUShort();
		mStat = new StyxStat(input);
	}
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	public StyxStat getStat()
	{
		if (mStat == null)
			return StyxStat.EMPTY;
		return mStat;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4
			+ getStat().getSize();
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getFID());
		output.writeUShort(getStat().getSize());
		getStat().writeBinaryTo(output);		
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nStat: %s", 
				getFID(), getStat().toString());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Twstat;
	}
	
}
