package com.v2soft.AndLib.networking;

import java.io.Closeable;
import java.net.SocketException;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public abstract class DiscoveryServer implements Closeable {
    public abstract void listen() throws SocketException;
}
