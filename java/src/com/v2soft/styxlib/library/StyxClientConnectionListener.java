package com.v2soft.styxlib.library;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public interface StyxClientConnectionListener {
    void onBeginConnection();
    void onGetVersion();
    void onAuth();
    void onAttached();
}
