package com.v2soft.styxlib.library;

public interface StyxClientManagerListener {
    void onBeginConnection();
    void onGetVersion();
    void onAuth();
    void onAttached();
}
