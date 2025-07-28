package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxRVersionMessage extends BaseMessage {
	public final long maxPacketSize;
	public final String protocolVersion;

	protected StyxRVersionMessage(long maxPacketSize, String protocolVersion) {
		super(MessageType.Rversion, Constants.NOTAG, null);
		this.maxPacketSize = maxPacketSize;
		this.protocolVersion = protocolVersion;
	}

}
