package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.types.ConnectionDetails;
import com.v2soft.styxlib.types.Credentials;

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
     * Connect to server with specified parameters
     * @return true if connected
     * @throws java.io.IOException
     * @throws com.v2soft.styxlib.exceptions.StyxException
     * @throws java.util.concurrent.TimeoutException
     */
    boolean connect(IChannelDriver driver)
            throws IOException, StyxException, InterruptedException, TimeoutException;
    /**
     * Connect to server with specified parameters
     * @param credentials user credentials
     * @return true if connected
     * @throws java.io.IOException
     * @throws com.v2soft.styxlib.exceptions.StyxException
     * @throws java.util.concurrent.TimeoutException
     */
    boolean connect(IChannelDriver driver, Credentials credentials)
            throws IOException, StyxException, InterruptedException, TimeoutException;
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

    IMessageTransmitter getTransmitter();

    int getTimeout();
    /**
     *
     * @return FID of root folder
     */
    long getRootFID();

    /**
     *
     * @return message recepient information
     */
    ClientDetails getRecepient();
}
