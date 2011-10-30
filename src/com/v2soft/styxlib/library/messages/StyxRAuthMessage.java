package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;

public class StyxRAuthMessage extends StyxMessage {
	private StyxQID mQID;

    public StyxRAuthMessage(int tag) {
        super(MessageType.Rauth, tag);
    }

//    public StyxRAuthMessage(StyxQID qid)
//	{
//		super(MessageType.Rauth);
//		mQID = qid;
//	}
//	
//	public StyxRAuthMessage(int tag, StyxQID qid)
//	{
//		super(MessageType.Rauth, tag);
//		mQID = qid;
//	}
	
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
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
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
	
}
