package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.l5.serialization.DataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.MessageSerializerImpl;
import com.v2soft.styxlib.l5.serialization.MessagesFactory;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public abstract class TCPChannelDriver implements IChannelDriver, Runnable {
    protected Thread mAcceptorThread;
    protected boolean isWorking;
    protected IMessageProcessor mTMessageHandler;
    protected IMessageProcessor mRMessageHandler;
    protected int mIOUnit;
    protected int mTransmittedPacketsCount;
    protected int mTransmissionErrorsCount;
    protected InetAddress mAddress;
    protected int mPort;
    protected MessagesFactory messagesFactory;
    protected DataSerializer serializer;

    public TCPChannelDriver(InetAddress address,
                            int port,
                            boolean ssl) throws IOException {
        mPort = port;
        mAddress = address;

        // Bind the server socket to the local host and port
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);

        prepareSocket(socketAddress, ssl);
        mTransmittedPacketsCount = 0;
        mTransmissionErrorsCount = 0;
        messagesFactory = new MessagesFactory();
        serializer = new MessageSerializerImpl();
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
        synchronized (client) {
            try {
                serializer.serialize(message, client.getOutputWriter());
                client.sendOutputBuffer();
                mTransmittedPacketsCount++;
                return true;
            } catch (IOException e) {
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
            read = client.getBufferLoader().readFromChannelToBuffer(client.getChannel());
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
        int inBuffer = client.getBuffer().remainsToRead();
        if ( inBuffer > 4 ) {
            long packetSize = client.getInputReader().getUInt32();
            if ( inBuffer >= packetSize ) {
                final StyxMessage message = messagesFactory.factory(client.getInputReader(), mIOUnit);
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

    public DataSerializer getSerializer() {
        return serializer;
    }
}
