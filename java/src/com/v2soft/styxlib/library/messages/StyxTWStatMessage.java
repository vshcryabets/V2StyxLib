package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;

public class StyxTWStatMessage extends StyxTMessage {
	private long mFID;
	private StyxStat mStat;

	public StyxTWStatMessage(long fid, StyxStat stat) {
		super(MessageType.Twstat);
		mFID = fid;
		mStat = stat;
	}
    @Override
    public void load(IStyxDataReader input) throws IOException {
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
			+ mStat.getSize();
	}
    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws IOException {
        super.writeToBuffer(output);
        output.writeUInt32(mFID);
        output.writeUInt16(mStat.getSize());
        mStat.writeBinaryTo(output);        
    }	

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nStat: %s", 
				getFID(), getStat().toString());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Twstat;
	}
}
