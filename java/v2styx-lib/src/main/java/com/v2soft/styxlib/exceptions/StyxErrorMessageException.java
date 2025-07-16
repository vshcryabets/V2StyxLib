package com.v2soft.styxlib.exceptions;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.messages.StyxRErrorMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.enums.MessageType;

import java.io.Serial;

/**
 *
 * @author V.Shcriabets (vshcryabets@gmail.com)
 *
 */
public class StyxErrorMessageException extends StyxException {
    @Serial
    private static final long serialVersionUID = 1;
    private final StyxRErrorMessage mMessage;

    public static StyxErrorMessageException newInstance(String message) {
        if (message == null)
            throw new NullPointerException("Message is null");
        return new StyxErrorMessageException(new StyxRErrorMessage(Constants.NOTAG, message), null);
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

    private StyxErrorMessageException(StyxRErrorMessage message, String fileName) {
        super(String.format("%s %s", message.mError, fileName));
        mMessage = message;
    }

    public StyxRErrorMessage getErrorMessage()
    {
        return mMessage;
    }

}
