package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public abstract class TCPChannelDriver implements IChannelDriver, Runnable {
    private static final String TAG = TCPChannelDriver.class.getSimpleName();
    protected Thread mThread;
    protected boolean isWorking;
    protected IMessageProcessor mTMessageHandler;
    protected IMessageProcessor mRMessageHandler;
    protected int mIOUnit;
    protected int mTransmittedPacketsCount;
    protected int mTransmissionErrorsCount;
    protected ILogListener mLogListener;
    protected String mAddress;
    protected int mPort;

    public TCPChannelDriver(String address, int port) {
        mPort = port;
        mAddress = address;
        mTransmittedPacketsCount = 0;
        mTransmissionErrorsCount = 0;
    }

    public abstract void prepareSocket() throws StyxException;
    public abstract void closeSocket() throws StyxException;

    protected int getTimeout() {
        return StyxServerManager.DEFAULT_TIMEOUT;
    }

    @Override
    public Thread start(int iounit) throws StyxException {
        if ( mThread != null ) {
            throw new IllegalStateException("Already started");
        }
        if (mTMessageHandler == null) {
            throw new IllegalStateException("mTMessageHandler not ready (is null)");
        }
        if (mRMessageHandler == null) {
            throw new IllegalStateException("mRMessageHandler not ready (is null)");
        }
        mIOUnit = iounit;
        prepareSocket();
        mThread = new Thread(this, "TcpDriver");
        mThread.start();
        isWorking = true;
        return mThread;
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
            mThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if ( mThread.isAlive() ) {
            mThread.interrupt();
        }
    }

    /**
     * Read data from assigned SocketChannel
     * @return true if socket was closed or other error happened.
     * @throws IOException in case of parse error.
     */
    protected boolean readSocket(TCPClientDetails client) throws IOException {
        int read = -1;
        try {
            read = client.getInputBuffer().readFromChannel(client.getChannel());
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
     * @throws IOException in case of parse error.
     */
    private boolean process(TCPClientDetails client) throws IOException {
        int inBuffer = client.getInputBuffer().remainsToRead();
        if ( inBuffer > 4 ) {
            long packetSize = client.getInputReader().getUInt32();
            if ( inBuffer >= packetSize ) {
                // whole packet are in input buffer
                final StyxMessage message = parseMessage(client.getInputReader());
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

    /**
     * Parse message from specified reader.
     * @param reader reader with buffer.
     * @return parsed message.
     */
    protected StyxMessage parseMessage(IStyxDataReader reader) throws IOException {
        return StyxMessage.factory(reader, mIOUnit);
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
