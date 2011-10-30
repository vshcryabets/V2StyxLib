package com.v2soft.styxlib.library.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ULong;

public class StyxTWriteMessage extends StyxTMessage {
	private long mFID;
	private ULong mOffset;
	private byte[] mData;

	public StyxTWriteMessage() throws IOException
	{
		this(NOFID, ULong.ZERO, null);
	}
	
	public StyxTWriteMessage(long fid, ULong offset, InputStream is) throws IOException
	{
		super(MessageType.Twrite);
		mFID = fid;
		mOffset = offset;
		setData(is);
	}
	
	public StyxTWriteMessage(int tag) throws IOException
	{
		this(tag, NOFID, ULong.ZERO, null);
	}
	
	public StyxTWriteMessage(int tag, long fid, ULong offset, InputStream is) throws IOException
	{
		super(MessageType.Twrite, tag);
		mFID = fid;
		mOffset = offset;
		setData(is);
	}
	
	@Override
	public void load(StyxInputStream input) throws IOException
	{
	    mFID = input.readUInt();
	    mOffset = input.readULong();
		int count = (int)input.readUInt();
		setData(input, 0, count);
	}
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	public ULong getOffset()
	{
		return mOffset;
	}
	
	public void setOffset(ULong offset)
	{
		mOffset = offset;
	}
	
	private byte[] getData()
	{
		if (mData == null)
			return new byte[0];
		return mData;
	}
	
	public InputStream getDataStream()
	{
		return new ByteArrayInputStream(getData());
	}
	
	public int getDataLength()
	{
		if (mData == null)
			return 0;
		return mData.length;
	}
	
	public void setData(InputStream is) throws IOException
	{
		if (is == null)
		{
			mData = null;
			return;
		}
		
		mData = new byte[is.available()];
		is.read(mData);
	}
	
	public void setData(InputStream is, int offset, int count) throws IOException
	{
		if (is == null)
		{
			mData = null;
			return;
		}
		
		mData = new byte[count];
		is.read(mData, offset, count);
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 16
			+ getDataLength();
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getFID());
		output.writeULong(getOffset());
		output.writeUInt(getDataLength());
		output.write(getData());		
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nOffset: %s\nData: %s",
				getFID(), getOffset().toString(), StyxMessage.toString(getData()));
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rwrite;
	}
	
}
