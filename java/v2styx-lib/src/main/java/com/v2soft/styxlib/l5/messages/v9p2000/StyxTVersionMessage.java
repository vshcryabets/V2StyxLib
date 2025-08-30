package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxTVersionMessage extends StyxTMessage {
	public final String protocolVersion;

	protected StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
        super(MessageType.Tversion, null, 0, maxPacketSize);
		this.protocolVersion = protocolVersion;
	}
}
