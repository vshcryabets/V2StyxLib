package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.Checks;
import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.impl.BufferReaderImpl;
import com.v2soft.styxlib.server.ClientsRepo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.List;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientChannelDriver extends TCPChannelDriver {
    protected TCPClientDetails mServerClientDetails;
    protected SocketChannel mChannel;

    public TCPClientChannelDriver(ClientsRepo clientsRepo) throws StyxException {
        super(clientsRepo);
    }

    @Override
    protected void prepareSocket(InetSocketAddress socketAddress, boolean ssl) throws StyxException {
        try {
            mChannel = SocketChannel.open(socketAddress);
            mChannel.configureBlocking(true);
            mServerClientDetails = new TCPClientDetails(mChannel, this, mInitConfiguration.iounit);
            mClientsRepo.addClient(mServerClientDetails);
            Socket socket = mChannel.socket();
            socket.setSoTimeout(getTimeout());
        } catch (Exception err) {
            throw new StyxException(err.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        if ( mServerClientDetails.getChannel() == null ) {
            return false;
        }
        return mServerClientDetails.getTcpChannel().isOpen();
    }

    @Override
    public boolean isStarted() {
        return isWorking;
    }

    @Override
    public void clearStatistics() {

    }

    @Override
    public void run() {
        try {
            isWorking = true;
            final BufferImpl buffer = new BufferImpl(mInitConfiguration.iounit*2);
            final IBufferReader reader = new BufferReaderImpl(buffer);
            while (isWorking) {
                if (Thread.interrupted()) break;
                // read from socket
                try {
                    int bytesRead = buffer.readFromChannelToBuffer(mServerClientDetails.getChannel());
                    if ( bytesRead > 0 ) {
                        // loop until we have unprocessed packets in the input buffer
                        while ( buffer.remainsToRead() > 4 ) {
                            // try to decode
                            final long packetSize = reader.getUInt32();
                            if ( buffer.remainsToRead() >= packetSize ) {
                                var message = mInitConfiguration.deserializer.deserializeMessage(reader, mInitConfiguration.iounit);
                                if ( Checks.isTMessage(message.type)) {
                                    mStartConfiguration.getTProcessor().onClientMessage(message, mServerClientDetails.getId());
                                } else {
                                    mStartConfiguration.getRProcessor().onClientMessage(message, mServerClientDetails.getId());
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
                catch (SocketTimeoutException e) {
                    // Nothing to read
                    e.printStackTrace();
                } catch (ClosedByInterruptException e) {
                    // finish
                    break;
                }
            }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            try {
                mServerClientDetails.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isWorking = false;
        }
    }

    @Override
    public void close() {
        super.close();
        mAcceptorThread.interrupt();
    }

    @Override
    public Collection<Integer> getClients() {
        return List.of(mServerClientDetails.getId());
    }

    @Override
    public String toString() {
        if (mChannel == null) {
            return String.format("%s:NULL", getClass().getSimpleName());
        }
        return String.format("%s:%s", getClass().getSimpleName(),
                mChannel.socket().getLocalAddress().toString());
    }
}
