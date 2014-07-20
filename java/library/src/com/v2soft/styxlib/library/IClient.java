package com.v2soft.styxlib.library;

import com.v2soft.styxlib.library.core.Messenger;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.IMessageTransmitter;

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
     * @throws com.v2soft.styxlib.library.exceptions.StyxException
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */
    public void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException;
    /**
     * Connect to server with specified parameters
     * @param username user name
     * @param password password
     * @return true if connected
     * @throws java.io.IOException
     * @throws com.v2soft.styxlib.library.exceptions.StyxException
     * @throws java.util.concurrent.TimeoutException
     */
    public boolean connect(IClientChannelDriver driver, String username, String password)
            throws IOException, StyxException, InterruptedException, TimeoutException;

    boolean isConnected();

    IMessageTransmitter getMessenger();

    int getTimeout();
    /**
     *
     * @return FID of root folder
     */
    long getFID();
    /**
     * Send TClunk message (release FID)
     * @param fid
     * @throws InterruptedException
     * @throws StyxException
     * @throws TimeoutException
     * @throws IOException
     */
    void releaseFID(long fid)
            throws InterruptedException, StyxException, TimeoutException, IOException;

    long allocateFID();

    int getIOBufSize();
}