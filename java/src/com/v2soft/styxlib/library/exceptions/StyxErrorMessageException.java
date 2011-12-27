package com.v2soft.styxlib.library.exceptions;

import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxErrorMessageException extends StyxException {
    private static final long serialVersionUID = 1;
    private StyxRErrorMessage mMessage;

    public static void doException(StyxMessage rMessage) 
            throws StyxErrorMessageException {
        if (rMessage == null)
            throw new NullPointerException();
        if (rMessage.getType() != MessageType.Rerror)
            return;
        StyxRErrorMessage rError = (StyxRErrorMessage) rMessage;
        throw new StyxErrorMessageException(rError);
    }

    public static void doException(StyxMessage rMessage, String fileName) 
            throws StyxErrorMessageException {
        if (rMessage == null)
            throw new NullPointerException();
        if (rMessage.getType() != MessageType.Rerror)
            return;

        StyxRErrorMessage rError = (StyxRErrorMessage) rMessage;
        throw new StyxErrorMessageException(rError, fileName);
    }


    private StyxErrorMessageException(StyxRErrorMessage message) {
        this(message, null);
    }

    private StyxErrorMessageException(StyxRErrorMessage message, String fileName) {
        super(String.format("%s %s", message.getError(), fileName));
        mMessage = message;
    }

    public StyxRErrorMessage getErrorMessage()
    {
        return mMessage;
    }

}
