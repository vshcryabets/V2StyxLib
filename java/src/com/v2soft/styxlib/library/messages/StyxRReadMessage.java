package com.v2soft.styxlib.library.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRReadMessage extends StyxMessage {
	private byte[] mData;
	
	public StyxRReadMessage(int tag, byte[] data) {
		super(MessageType.Rread, tag);
		mData = data;
	}
	
    @Override
    public void load(StyxBufferOperations input) 
        throws IOException  {
        int count = (int)input.readUInt32();
        mData = new byte[count];
        input.read(mData, 0, count);
    }
	
	private byte[] getData()
	{
		if (mData == null)
			return new byte[0];
		return mData;
	}
	
	public InputStream getDataStream()	{
		// TODO Optimize me
		return new ByteArrayInputStream(getData());
	}
	
	public int getDataLength()
	{
		if (mData == null)
			return 0;
		return mData.length;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4
			+ getDataLength();
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
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
