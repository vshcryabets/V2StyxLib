package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

public class StyxTVersionMessage extends StyxTMessage {
	private long mMaxPacketSize;
	private String mProtocolVersion;

	public StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
        super(MessageType.Tversion, MessageType.Rversion);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}

    @Override
    public void load(BufferReader input)
        throws IOException {
        super.load(input);
		mMaxPacketSize = input.readUInt32();
		mProtocolVersion = input.readUTFString();
    }

	public long getMaxPacketSize()
	{
		return mMaxPacketSize;
	}

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
