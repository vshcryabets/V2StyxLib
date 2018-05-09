package com.v2soft.styxlib.exceptions;

public class StyxException extends Exception {

    private static final long serialVersionUID = 5801949049418471796L;
    public static final int NONE = 0x0;
    public static final int DRIVER_CREATE_ERROR = 0x1000;
    public static final int DRIVER_BIND_ERROR = 0x1001;
    public static final int DRIVER_CONFIGURE_ERROR = 0x1002;
    public static final int DRIVER_CANT_RESOLVE_NAME = 0x1003;

    private int mInternalCode = NONE;

    public StyxException(int code) {
        super();
        mInternalCode = code;
    }

    public StyxException(String message) {
        super(message);
    }

    public int getInternalCode() {
        return mInternalCode;
    }
}
