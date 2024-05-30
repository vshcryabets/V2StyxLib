package com.v2soft.styxlib.exceptions;

import java.io.Serial;

public class StyxException extends Exception {

    @Serial
    private static final long serialVersionUID = 5801949049418471796L;

    public StyxException() {
        super();
    }
    public StyxException(String message) {
        super(message);
    }
}
