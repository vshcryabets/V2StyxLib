package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
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

    public TCPChannelDriver(InetAddress address, int port) throws IOException {
        mPort = port;
        mAddress = address;
        mTransmittedPacketsCount = 0;
        mTransmissionErrorsCount = 0;
    }

    public abstract void prepareSocket() throws IOException;

    protected int getTimeout() {
        return StyxServerManager.DEFAULT_TIMEOUT;
    }

    @Override
    public Thread start(int iounit) throws IOException {
        if ( mAcceptorThread != null ) {
            throw new IllegalStateException("Already started");
        }
        if (mTMessageHandler == null) {
            throw new IllegalStateException("mTMessageHandler not ready (is null)");
        }
        if (mRMessageHandler == null) {
            throw new IllegalStateException("mRMessageHandler not ready (is null)");
        }
        prepareSocket();
        mIOUnit = iounit;
        mAcceptorThread = new Thread(this, "TcpDriver");
        mAcceptorThread.start();
        isWorking = true;
        return mAcceptorThread;
    }

    @Override
    public boolean sendMessage(StyxMessage message, ClientDetails recipient) {
        if ( recipient == null ) {
            throw new NullPointerException("Recipient can't be null");
        }
        TCPClientDetails client = (TCPClientDetails) recipient;
        synchronized (client) {
            try {
                message.writeToBuffer(client.getOutputWriter());
                ByteBuffer buffer = client.getOutputBuffer();
                buffer.flip();
                client.getSocket().write(buffer);
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

    @Override
    public void setTMessageHandler(IMessageProcessor handler) {
        mTMessageHandler = handler;
    }

    @Override
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
        int read = -1;
        try {
            read = client.getInputBuffer().readFromChannel(client.getSocket());
        }
        catch (IOException e) {
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
                final StyxMessage message = StyxMessage.factory(client.getInputReader(), mIOUnit);
                if ( mLogListener != null ) {
                    mLogListener.onMessageReceived(this, client, message);
                }
                if ( message.getType().isTMessage() ) {
                    mTMessageHandler.postPacket(message, client);
                } else {
                    mRMessageHandler.postPacket(message, client);
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
