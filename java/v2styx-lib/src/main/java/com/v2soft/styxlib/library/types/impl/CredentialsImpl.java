package com.v2soft.styxlib.library.types.impl;

import com.v2soft.styxlib.library.types.Credentials;

public record CredentialsImpl(
        String mUserName,
        String mPassword
) implements Credentials {

    @Override
    public String getUserName() {
        return mUserName;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }
}
