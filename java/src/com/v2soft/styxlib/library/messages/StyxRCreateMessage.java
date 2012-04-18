package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRCreateMessage extends StyxMessage {
	private StyxQID mQID;
	private long mIOUnit;

	public StyxRCreateMessage(int tag, StyxQID qid, int iounit)
	{
		this(tag);
		mQID = qid;
		mIOUnit = iounit;
	}
	
	public StyxRCreateMessage(int tag) {
        super(MessageType.Rcreate, tag);
    }

    @Override
    public void load(StyxBufferOperations input) throws IOException {
        mQID = new StyxQID(input);
        mIOUnit = input.readUInt32();
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
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		getQID().writeBinaryTo(output);
		output.writeUInt(getIOUnit());		
	}

	@Override
	protected String internalToString() {
		return String.format("QID: %s\nIOUnit: %d", 
				getQID().toString(), getIOUnit());
	}
	
}
