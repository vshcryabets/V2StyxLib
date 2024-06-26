package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

public class StyxTVersionMessage extends StyxTMessage {
	private final long mMaxPacketSize;
	private final String mProtocolVersion;

	public StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
        super(MessageType.Tversion, MessageType.Rversion);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
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
    public String toString() {
		return String.format("%s\nMaxPocketSize: %d;\nProtocolVersion: %s",
		        super.toString(),
				getMaxPacketSize(), getProtocolVersion());
	}
}
