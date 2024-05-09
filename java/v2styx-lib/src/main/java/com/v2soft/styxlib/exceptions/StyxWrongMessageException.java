package com.v2soft.styxlib.exceptions;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.enums.MessageType;

import java.io.Serial;

public class StyxWrongMessageException extends StyxException {

    @Serial
    private static final long serialVersionUID = 9071135844358484201L;

    public StyxWrongMessageException(StyxMessage received, MessageType needed) {
        super(String.format("Recived massage of type %s when needed %s.",
                received.getType().toString(), needed.toString()));
    }

}
