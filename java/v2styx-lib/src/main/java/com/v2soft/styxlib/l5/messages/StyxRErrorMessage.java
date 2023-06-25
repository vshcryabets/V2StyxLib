package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.IStyxDataReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

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
		return super.getBinarySize() + UTF.getUTFSize(getError());
	}

	@Override
	public String toString() {
	    return String.format("%s\nError: %s", super.toString(), getError());
	}
}
