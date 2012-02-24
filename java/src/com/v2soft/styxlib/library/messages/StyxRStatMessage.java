package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.DualStateBuffer;

public class StyxRStatMessage extends StyxMessage {
	private StyxStat mStat;
    private int mDevice;

	public StyxRStatMessage(StyxStat stat)
	{
		super(MessageType.Rstat);
		mStat = stat;
	}
	
	public StyxRStatMessage(int tag, StyxStat stat) {
		this(tag);
		mStat = stat;
	}
	
	public StyxRStatMessage(int tag) {
	    super(MessageType.Rstat, tag);
    }

	@Override
    public void load(StyxInputStream input) throws IOException {
		int size = input.readUInt16();
		mStat = new StyxStat(input);
	}
    @Override
    public void load(DualStateBuffer input) throws IOException {
        int size = input.readUInt16();
        mStat = new StyxStat(input);
    }
	
	public StyxStat getStat()
	{
		if (mStat == null)
			return StyxStat.EMPTY;
		return mStat;
	}
	
	public void setStat(StyxStat stat)
	{
		mStat = stat;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize()
			+ getStat().getSize();
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException
	{
		output.writeUShort(getStat().getSize());
		getStat().writeBinaryTo(output);		
	}

	@Override
	protected String internalToString() {
		return String.format("Stat: %s", getStat().toString());
	}
	
}
