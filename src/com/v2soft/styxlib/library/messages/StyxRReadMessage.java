package com.v2soft.styxlib.library.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRReadMessage extends StyxMessage {
	private byte[] mData;
	
	public StyxRReadMessage() throws IOException
	{
		this(null);
	}
	
	public StyxRReadMessage(InputStream is) throws IOException
	{
		super(MessageType.Rread);
		setData(is);
	}
	
	public StyxRReadMessage(int tag) throws IOException
	{
		this(tag, null);
	}
	
	public StyxRReadMessage(int tag, InputStream is) throws IOException
	{
		super(MessageType.Rread, tag);
		setData(is);
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
        int count = (int)input.readUInt();
		setData(input, 0, count);
	}
	
	private byte[] getData()
	{
		if (mData == null)
			return new byte[0];
		return mData;
	}
	
	public InputStream getDataStream()	{
		// TODO WTF????
		return new ByteArrayInputStream(getData());
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
	
	public int getDataLength()
	{
		if (mData == null)
			return 0;
		return mData.length;
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
		return super.getBinarySize() + 4
			+ getDataLength();
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getDataLength());
		output.write(getData());
	}

	@Override
	protected String internalToString() {
	    if ( Config.LOG_DATA_FIELDS ) {
    		return String.format("Data Length:%d\nData: %s",
    		        mData.length,
    				StyxMessage.toString(mData));
	    } else {
            return String.format("Data Length:%d",
                    mData.length);
	    }
	}
	
}
