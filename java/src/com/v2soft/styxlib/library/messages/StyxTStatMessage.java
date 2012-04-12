package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

/**
 * Class for TStat message
 * @author vhscryabets@gmail.com
 *
 */
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
    public void load(StyxBufferOperations input) 
        throws IOException  {
        setFID(input.readUInt32());
    }
	
    /**
     * @return File ID
     */
	public long getFID() {return mFID;}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4;
	}
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
	    output.writeUInt(getFID());
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d", getFID());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rstat;
	}
	
}
