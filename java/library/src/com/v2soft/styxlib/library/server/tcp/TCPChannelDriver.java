package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.core.IMessageProcessor;
import com.v2soft.styxlib.library.io.StyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public abstract class TCPChannelDriver implements IChannelDriver, Runnable {
    private static final String TAG = TCPChannelDriver.class.getSimpleName();
    private int mPort;
    private boolean mSSL;
    private boolean mNeedAuth;
    private Thread mAcceptorThread;
    protected boolean isWorking;
    protected IMessageProcessor mMessageHandler;
    protected int mIOUnit;
    protected int mTransmitedPacketsCount;
    protected int mTransmissionErrorsCount;

    public TCPChannelDriver(InetAddress address, int port, boolean ssl, int IOUnit) throws IOException {
        mPort = port;
        mIOUnit = IOUnit;

        // Bind the server socket to the local host and port
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);

        prepareSocket(socketAddress, ssl);
        mTransmitedPacketsCount = 0;
        mTransmissionErrorsCount = 0;
    }

    protected abstract void prepareSocket(InetSocketAddress socketAddress, boolean ssl) throws IOException;

    protected int getTimeout() {
        return StyxServerManager.DEFAULT_TIMEOUT;
    }

    @Override
    public Thread start() {
        if ( mMessageHandler == null ) {
            throw new IllegalStateException("Message handler is null");
        }
        mAcceptorThread = new Thread(this, TAG);
        mAcceptorThread.start();
        return mAcceptorThread;
    }

    @Override
    public boolean sendMessage(StyxMessage message) {
        ClientState client = (ClientState) message.getRouteInfo();
        if ( client == null ) {
            throw new NullPointerException("Client can't be null");
        }
        ByteBuffer buffer = ((TCPClientState)client).getOutputBuffer();
        try {
            message.writeToBuffer(new StyxDataWriter(buffer));
            buffer.position(0);
            ((TCPClientState)client).getChannel().write(buffer);
            mTransmitedPacketsCount++;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            mTransmissionErrorsCount++;
        }
        return false;
    }

    public void setMessageHandler(IMessageProcessor handler) {
        mMessageHandler = handler;
    }

    @Override
    public void close() {
        isWorking= false;
    }

    /**
     * Read data from assigned SocketChannel
     * @return
     * @throws IOException
     */
    public boolean readSocket(TCPClientState client) throws IOException {
        int read = 0;
        try {
            read = client.getInputBuffer().readFromChannel(client.getChannel());
        }
        catch (IOException e) {
            read = -1;
        }
        if ( read == -1 ) {
            close();
            return true;
        } else {
            while ( process(client) );
        }
        return false;
    }

    /**
     *
     * @return true if message was processed
     * @throws IOException
     */
    private boolean process(TCPClientState client) throws IOException {
        int inBuffer = client.getInputBuffer().remainsToRead();
        if ( inBuffer > 4 ) {
            long packetSize = client.getInputReader().getUInt32();
            if ( inBuffer >= packetSize ) {
                final StyxMessage message = StyxMessage.factory(client.getInputReader(), mIOUnit);
                message.setRouteInfo(client);
                mMessageHandler.processPacket(message);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getTransmitedCount() {
        return mTransmitedPacketsCount;
    }
    @Override
    public int getErrorsCount() {
        return mTransmissionErrorsCount;
    }
}
