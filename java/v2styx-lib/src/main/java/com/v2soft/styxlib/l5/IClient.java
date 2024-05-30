package com.v2soft.styxlib.l5;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.library.types.ConnectionDetails;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by V.Shcryabets on 6/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IClient extends Closeable {
    /**
     * Restart session with server
     *
     * @throws InterruptedException
     * @throws com.v2soft.styxlib.exceptions.StyxException
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */
    void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException;
    /**
     * Connect to server.
     * @return true if connected
     * @throws java.io.IOException
     * @throws com.v2soft.styxlib.exceptions.StyxException
     * @throws java.util.concurrent.TimeoutException
     */
    boolean connect()
            throws IOException, StyxException, InterruptedException, TimeoutException;

    boolean isConnected();

    IMessageTransmitter getMessenger();

    int getTimeout();
    /**
     * @return FID of root folder
     */
    long getRootFID();

    ConnectionDetails getConnectionDetails();

    /**
     * @return message recepient information
     */
    ClientDetails getRecepient();
}
