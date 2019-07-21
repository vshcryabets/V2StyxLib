package com.v2soft.styxlib.types;

/**
 * Created by mrco on 11/2/14.
 */
public class Credentials {
    protected String mUserName;
    protected String mPassword;

    public Credentials(String username, String password) {
        mUserName = username;
        mPassword = password;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getPassword() {
        return mPassword;
    }
}
