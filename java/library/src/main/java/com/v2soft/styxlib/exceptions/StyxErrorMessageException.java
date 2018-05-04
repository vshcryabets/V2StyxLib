package com.v2soft.styxlib.exceptions;

import com.v2soft.styxlib.messages.StyxRErrorMessage;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.enums.MessageType;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxErrorMessageException extends StyxException {
    private static final long serialVersionUID = 1;
    private StyxRErrorMessage mMessage;

    public static void checkException(StyxMessage rMessage)
            throws StyxErrorMessageException {
        if (rMessage == null)
            throw new NullPointerException();
        if (rMessage.getType() != MessageType.Rerror)
            return;
        StyxRErrorMessage rError = (StyxRErrorMessage) rMessage;
        throw new StyxErrorMessageException(rError);
    }

    public static StyxErrorMessageException newInstance(String message) {
        if (message == null)
            throw new NullPointerException("Message is null");
        return new StyxErrorMessageException(new StyxRErrorMessage(StyxMessage.NOTAG, message));
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
