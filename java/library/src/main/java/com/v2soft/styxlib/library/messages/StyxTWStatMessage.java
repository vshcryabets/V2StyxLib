package com.v2soft.styxlib.library.messages;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;

import java.io.IOException;

public class StyxTWStatMessage extends StyxTMessageFID {
	private StyxStat mStat;

	public StyxTWStatMessage(long fid, StyxStat stat) {
		super(MessageType.Twstat, MessageType.Rwstat, fid);
		mStat = stat;
	}
    @Override
    public void load(IStyxDataReader input) throws IOException {
        super.load(input);
        input.readUInt16();
        mStat = new StyxStat(input);
    }
	
	public StyxStat getStat()
	{
		if (mStat == null)
			return StyxStat.EMPTY;
		return mStat;
	}
	
	@Override
	public int getBinarySize() {
	    return super.getBinarySize()
			+ mStat.getSize();
	}
    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws IOException {
        super.writeToBuffer(output);
        output.writeUInt16(mStat.getSize());
        mStat.writeBinaryTo(output);        
    }	

	@Override
    public String toString() {
		return String.format("%s\nStat: %s", 
				super.toString(), getStat().toString());
	}
}
