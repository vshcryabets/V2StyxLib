package com.v2soft.styxlib.exceptions;

/**
 *
 * @author V.Shcriabets (vshcryabets@gmail.com)
 *
 */
public class StyxErrorMessageException extends StyxException {
    public static StyxErrorMessageException newInstance(String message) {
        if (message == null)
            throw new NullPointerException("Message is null");
        return new StyxErrorMessageException(message, null);
    }
    private StyxErrorMessageException(String message, String fileName) {
        super(String.format("%s %s", message, fileName == null ? "[No File]" : fileName));
    }
}
