package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.enums.MessageType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxRErrorMessage extends StyxMessage {
	private String mError;

	public StyxRErrorMessage(int tag, String error) {
		super(MessageType.Rerror, tag);
		mError = error;
	}

    @Override
    public void load(IStyxDataReader input)
        throws IOException  {
        setError(input.readUTFString());
    }
	public String getError() {
		return mError;
	}
	public void setError(String error) {
		mError = error;
	}

	@Override
	public int getBinarySize() {
		return super.getBinarySize()
			+ StyxMessage.getUTFSize(getError());
	}

	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUTFString(getError());
	}
	@Override
	public String toString() {
	    return String.format("%s\nError: %s", super.toString(), getError());
	}
}
