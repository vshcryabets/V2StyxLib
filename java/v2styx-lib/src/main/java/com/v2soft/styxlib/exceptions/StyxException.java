package com.v2soft.styxlib.exceptions;

import java.io.IOException;

public class StyxException extends IOException {
    public StyxException() {
        super();
    }
    public StyxException(String message) {
        super(message);
    }
}
