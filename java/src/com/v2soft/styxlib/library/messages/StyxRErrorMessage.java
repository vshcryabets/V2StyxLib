package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRErrorMessage extends StyxMessage 
{
	private String mError;
	
	public StyxRErrorMessage()
	{
		super(MessageType.Rerror);
	}
	
	public StyxRErrorMessage(int tag)
	{
		super(MessageType.Rerror, tag);
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
        setError(input.readUTF());
	}
    @Override
    public void load(DualStateBuffer input) 
        throws IOException  {
        setError(input.readUTF());
    }	
	public String getError()
	{
		if (mError == null)
			return "";
		return mError;
	}
	
	public void setError(String error)
	{
		mError = error;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize()
			+ StyxMessage.getUTFSize(getError());
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUTF(getError());
	}

	@Override
	protected String internalToString() {
		return String.format("Error: %s", getError());
	}

}
