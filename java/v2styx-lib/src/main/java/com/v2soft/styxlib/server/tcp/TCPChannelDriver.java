package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.Checks;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.utils.Future;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public abstract class TCPChannelDriver implements
        IChannelDriver<TCPChannelDriver.InitConfiguration>,
        Runnable {
    public static class InitConfiguration extends IChannelDriver.InitConfiguration {
        public final boolean ssl;
        public final InetAddress address;
        public final int port;

        public InitConfiguration(IDataSerializer serializer,
                                 IDataDeserializer deserializer,
                                 int iounit,
                                 boolean ssl,
                                 InetAddress address,
                                 int port
        ) {
            super(serializer, deserializer, iounit);
            this.ssl = ssl;
            this.address = address;
            this.port = port;
        }
    }

    protected Thread mAcceptorThread;
    protected boolean isWorking;
    protected int mTransmittedPacketsCount;
    protected int mTransmissionErrorsCount;
    protected ClientsRepo mClientsRepo;
    protected InitConfiguration mInitConfiguration;
    protected StartConfiguration mStartConfiguration;

    public TCPChannelDriver(ClientsRepo clientsRepo) throws StyxException {
        mClientsRepo = clientsRepo;
        mTransmittedPacketsCount = 0;
        mTransmissionErrorsCount = 0;
    }

    @Override
    public void prepare(InitConfiguration configuration) {
        mInitConfiguration = configuration;
    }

    protected abstract void prepareSocket(InetSocketAddress socketAddress, boolean ssl) throws StyxException;

    protected int getTimeout() {
        return StyxServerManager.DEFAULT_TIMEOUT;
    }

    @Override
    public Thread start(StartConfiguration configuration) throws StyxException {
        if (mInitConfiguration == null)
            throw new StyxException("Not initialized");
        if (mAcceptorThread != null) {
            throw new IllegalStateException("Already started");
        }
        mStartConfiguration = configuration;
        // Bind the server socket to the local host and port
        var socketAddress = new InetSocketAddress(mInitConfiguration.address, mInitConfiguration.port);
        prepareSocket(socketAddress, mInitConfiguration.ssl);
        mAcceptorThread = new Thread(this, toString());
        mAcceptorThread.start();
        isWorking = true;
        return mAcceptorThread;
    }

    @Override
    public <R extends StyxMessage> Future<R> sendMessage(
            StyxMessage message,
            int clientId,
            long timeout) throws StyxException {
        if (clientId < 0) {
            throw new StyxException("Client id is negative");
        }
        final var client = (TCPClientDetails) mClientsRepo.getClient(clientId);
        try {
            mInitConfiguration.serializer.serialize(message, client.getOutputWriter());
            client.sendOutputBuffer();
            mTransmittedPacketsCount++;
        } catch (StyxException e) {
            mTransmissionErrorsCount++;
            throw e;
        }
        return new Future<>(CompletableFuture.supplyAsync(() -> {
            try {
                return (R) ((StyxTMessage) message).waitForAnswer(timeout);
            } catch (StyxException e) {
                throw new CompletionException(e);
            }
        }));

    }

    @Override
    public void close() {
        isWorking = false;
        try {
            mAcceptorThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAcceptorThread.isAlive()) {
            mAcceptorThread.interrupt();
        }
    }

    /**
     * Read data from assigned SocketChannel
     *
     * @throws IOException
     */
    protected boolean readSocket(int clientId) throws StyxException {
        int read = 0;
        final var client = (TCPClientDetails) mClientsRepo.getClient(clientId);
        try {
            read = client.getBufferLoader().readFromChannelToBuffer(client.getChannel());
        } catch (IOException e) {
            read = -1;
        }
        if (read == -1) {
            return true;
        } else {
            while (process(clientId)) ;
        }
        return false;
    }

    /**
     * Read income message from specified client.
     *
     * @return true if message was processed
     */
    private boolean process(int clientId) throws StyxException {
        final var client = (TCPClientDetails) mClientsRepo.getClient(clientId);
        int inBuffer = client.getBuffer().remainsToRead();
        if (inBuffer > 4) {
            long packetSize = client.getInputReader().getUInt32();
            if (inBuffer >= packetSize) {
                var message = mInitConfiguration.deserializer.deserializeMessage(client.getInputReader(),
                        mInitConfiguration.iounit);
                if (Checks.isTMessage(message.type)) {
                    mStartConfiguration.getTProcessor().onClientMessage(message, clientId);
                } else {
                    mStartConfiguration.getRProcessor().onClientMessage(message, clientId);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mInitConfiguration.address.hashCode() * mInitConfiguration.port;
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
        if (mInitConfiguration == null) {
            return String.format("%s:uninitialized", getClass().getSimpleName());
        }
        return String.format("%s:%s:%d", getClass().getSimpleName(),
                mInitConfiguration.address.toString(),
                mInitConfiguration.port);
    }

//    public int getPort() {
//        return mPort;
//    }

//    @Override
//    public IDataSerializer getSerializer() {
//        return serializer;
//    }
//
//    @Override
//    public IDataDeserializer getDeserializer() {
//        return deserializer;
//    }
}
