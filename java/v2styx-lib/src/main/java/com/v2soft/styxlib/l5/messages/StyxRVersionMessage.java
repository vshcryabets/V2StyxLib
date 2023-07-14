package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

public class StyxRVersionMessage extends StyxMessage {
	private long mMaxPacketSize;
	private String mProtocolVersion;

	public StyxRVersionMessage(long maxPacketSize, String protocolVersion) {
		super(MessageType.Rversion, StyxMessage.NOTAG);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}

    @Override
    public void load(BufferReader input)
        throws IOException  {
		mMaxPacketSize = input.readUInt32();
		mProtocolVersion = input.readUTFString();
    }

    // TODO should max packet size be long? or int?
	public long getMaxPacketSize() {return mMaxPacketSize;}

	public String getProtocolVersion()
	{
		if (mProtocolVersion == null)
			return "";
		return mProtocolVersion;
	}

	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4
			+ UTF.getUTFSize(getProtocolVersion());
	}

	@Override
	public String toString() {
        return String.format("%s\nMaxPocketSize: %d;\nProtocolVersion: %s",
                super.toString(),
                getMaxPacketSize(), getProtocolVersion());
	}
}
