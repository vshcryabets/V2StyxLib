package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.l5.serializtion.MessagesFactory;
import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

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
    protected Thread mAcceptorThread;
    protected boolean isWorking;
    protected IMessageProcessor mTMessageHandler;
    protected IMessageProcessor mRMessageHandler;
    protected int mIOUnit;
    protected int mTransmittedPacketsCount;
    protected int mTransmissionErrorsCount;
    protected ILogListener mLogListener;
    protected InetAddress mAddress;
    protected int mPort;
    protected MessagesFactory messagesFactory;

    public TCPChannelDriver(InetAddress address, int port, boolean ssl) throws IOException {
        mPort = port;
        mAddress = address;

        // Bind the server socket to the local host and port
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);

        prepareSocket(socketAddress, ssl);
        mTransmittedPacketsCount = 0;
        mTransmissionErrorsCount = 0;
        messagesFactory = new MessagesFactory();
    }

    protected abstract void prepareSocket(InetSocketAddress socketAddress, boolean ssl) throws IOException;

    protected int getTimeout() {
        return StyxServerManager.DEFAULT_TIMEOUT;
    }

    @Override
    public Thread start(int iounit) {
        if ( mAcceptorThread != null ) {
            throw new IllegalStateException("Already started");
        }
        mIOUnit = iounit;
        mAcceptorThread = new Thread(this, toString());
        mAcceptorThread.start();
        isWorking = true;
        return mAcceptorThread;
    }

    @Override
    public boolean sendMessage(StyxMessage message, ClientDetails recipient) {
        if ( recipient == null ) {
            throw new NullPointerException("Client can't be null");
        }
        TCPClientDetails client = (TCPClientDetails) recipient;
        ByteBuffer buffer = client.getOutputBuffer();
        synchronized (client) {
            try {
                message.writeToBuffer(client.getOutputWriter());
                buffer.flip();
                client.getChannel().write(buffer);
                mTransmittedPacketsCount++;
                if (mLogListener != null) {
                    mLogListener.onMessageTransmited(this, recipient, message);
                }
                return true;
            } catch (IOException e) {
                if (mLogListener != null) {
                    mLogListener.onException(this, e);
                }
                mTransmissionErrorsCount++;
            }
        }
        return false;
    }

    public void setTMessageHandler(IMessageProcessor handler) {
        mTMessageHandler = handler;
    }

    public void setRMessageHandler(IMessageProcessor handler) {
        mRMessageHandler = handler;
    }

    @Override
    public void close() {
        isWorking = false;
        try {
            mAcceptorThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if ( mAcceptorThread.isAlive() ) {
            mAcceptorThread.interrupt();
        }
    }

    /**
     * Read data from assigned SocketChannel
     * @throws IOException
     */
    protected boolean readSocket(TCPClientDetails client) throws IOException {
        int read = 0;
        try {
            read = client.getInputBuffer().readFromChannel(client.getChannel());
        }
        catch (IOException e) {
            read = -1;
        }
        if ( read == -1 ) {
            return true;
        } else {
            while ( process(client) );
        }
        return false;
    }

    /**
     * Read income message from specified client.
     * @return true if message was processed
     * @throws IOException
     */
    private boolean process(TCPClientDetails client) throws IOException {
        int inBuffer = client.getInputBuffer().remainsToRead();
        if ( inBuffer > 4 ) {
            long packetSize = client.getInputReader().getUInt32();
            if ( inBuffer >= packetSize ) {
                final StyxMessage message = messagesFactory.factory(client.getInputReader(), mIOUnit);
                if ( mLogListener != null ) {
                    mLogListener.onMessageReceived(this, client, message);
                }
                if ( message.getType().isTMessage() ) {
                    if ( mTMessageHandler != null ) {
                        mTMessageHandler.postPacket(message, client);
                    }
                } else {
                    if ( mRMessageHandler != null ) {
                        mRMessageHandler.postPacket(message, client);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mAddress.hashCode()*mPort;
    }

    @Override
    public int getTransmittedCount() {
        return mTransmittedPacketsCount;
    }
    @Override
    public int getErrorsCount() {
        return mTransmissionErrorsCount;
    }

    @Override
    public void setLogListener(ILogListener listener) {
        mLogListener = listener;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%d", getClass().getSimpleName(), mAddress.toString(), mPort);
    }

    @Override
    public IMessageProcessor getTMessageHandler() {
        return mTMessageHandler;
    }

    @Override
    public IMessageProcessor getRMessageHandler() {
        return mRMessageHandler;
    }

    public int getPort() {
        return mPort;
    }
}
