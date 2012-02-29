package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRAuthMessage extends StyxMessage {
	private StyxQID mQID;

    public StyxRAuthMessage(int tag) {
        super(MessageType.Rauth, tag);
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
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + StyxQID.CONTENT_SIZE;
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		getQID().writeBinaryTo(output);
	}

	@Override
	protected String internalToString() {
		return String.format("QID: %s", getQID().toString());
	}

    @Override
    protected void load(StyxInputStream is) throws IOException {
        mQID = new StyxQID(is);
    }
    @Override
    protected void load(DualStateBuffer is) throws IOException {
        mQID = new StyxQID(is);
    }	
}
