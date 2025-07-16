package com.v2soft.styxlib.l5.messages.v9p2000;

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
}
