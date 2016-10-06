package com.v2soft.styxlib.types;

/**
 * Created by vshcryabets on 11/13/14.
 */
public class ConnectionDetails {
    protected String mProtocol;
    protected int mIOUnit;

    public ConnectionDetails(String protocol, int iounit) {
        mProtocol = protocol;
        mIOUnit = iounit;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public int getIOUnit() {
        return mIOUnit;
    }

}
