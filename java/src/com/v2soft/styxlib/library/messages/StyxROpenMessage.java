package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;

public class StyxROpenMessage extends StyxMessage {
	private StyxQID mQID;
	private long mIOUnit;

	public StyxROpenMessage(int tag, StyxQID qid, long iounit) {
		super(MessageType.Ropen, tag);
		mQID = qid;
		mIOUnit = iounit;
	}
	
    @Override
    public void load(IStyxDataReader stream) throws IOException {
        mQID = new StyxQID(stream);
        mIOUnit = stream.readUInt32();
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
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		mQID.writeBinaryTo(output);
		output.writeUInt32(mIOUnit);
	}

	@Override
	protected String internalToString() {
		return String.format("QID: %s\nIOUnit: %d", 
				mQID.toString(), getIOUnit());
	}
	
}
