package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

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
    public void load(DualStateBuffer input) throws IOException {
        mFID = input.readUInt32();
        input.readUInt16();
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
    public void writeToBuffer(StyxBufferOperations output)
            throws IOException {
        super.writeToBuffer(output);
        output.writeUInt(getFID());
        output.writeUShort(getStat().getSize());
        getStat().writeBinaryTo(output);        
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

    @Override
    protected void load(StyxInputStream is) throws IOException {
    }
	
}
