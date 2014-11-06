package com.v2soft.AndLib.networking;

import java.io.Closeable;
import java.net.SocketException;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public abstract class DiscoveryClient {

    public DiscoveryClient() {
    }

    public abstract void startDiscoverySync();

    /**
     *
     * @return number of broadcast requests.
     */
    public abstract int getRetryCount();
    /**
     *
     * @return deleay in miliseconds between broadcast requests.
     */
    public abstract int getDelayBetweenRetry();
}
