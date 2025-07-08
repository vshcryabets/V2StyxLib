package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;

public class StyxTVersionMessage extends StyxTMessage {
	public final long maxPacketSize;
	public final String protocolVersion;

	public StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
        super(MessageType.Tversion, MessageType.Rversion);
		this.maxPacketSize = maxPacketSize;
		this.protocolVersion = protocolVersion;
	}

	@Override
    public String toString() {
		return String.format("%s\nMaxPocketSize: %d;\nProtocolVersion: %s",
		        super.toString(),
				maxPacketSize,
				protocolVersion);
	}
}
