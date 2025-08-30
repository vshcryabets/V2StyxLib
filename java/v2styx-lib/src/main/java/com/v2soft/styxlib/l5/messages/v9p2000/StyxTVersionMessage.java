package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.QID;

public class StyxTVersionMessage extends StyxTMessage {

	protected StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
        super(MessageType.Tversion, QID.EMPTY, 0, maxPacketSize, protocolVersion);
	}
}
