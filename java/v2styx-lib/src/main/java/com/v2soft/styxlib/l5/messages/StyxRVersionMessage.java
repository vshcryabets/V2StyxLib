package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;

public class StyxRVersionMessage extends StyxMessage {
	public final long maxPacketSize;
	public final String protocolVersion;

	public StyxRVersionMessage(long maxPacketSize, String protocolVersion) {
		super(MessageType.Rversion, Constants.NOTAG);
		this.maxPacketSize = maxPacketSize;
		this.protocolVersion = protocolVersion;
	}

	@Override
	public String toString() {
        return String.format("%s\nMaxPocketSize: %d;\nProtocolVersion: %s",
                super.toString(),
                maxPacketSize, protocolVersion);
	}
}
