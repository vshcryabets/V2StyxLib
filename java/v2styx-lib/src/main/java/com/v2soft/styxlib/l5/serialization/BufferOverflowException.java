package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;

public class BufferOverflowException extends StyxException {
    public BufferOverflowException(){
        super("Buffer overflow");
    }

    public BufferOverflowException(String message) {
        super(message);
    }
}
