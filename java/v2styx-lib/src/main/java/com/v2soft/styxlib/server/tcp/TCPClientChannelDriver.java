package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.Checks;
import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.impl.BufferReaderImpl;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientChannelDriver extends TCPChannelDriver {
    public static final int PSEUDO_CLIENT_ID = 1;
    protected TCPClientDetails mServerClientDetails;
    protected SocketChannel mChanel;

    public TCPClientChannelDriver(InetAddress address, int port, boolean ssl) throws StyxException {
        super(address, port, ssl);
    }

    @Override
    public Thread start(int iounit) {
        mServerClientDetails = new TCPClientDetails(mChanel, this, iounit, PSEUDO_CLIENT_ID);
        return super.start(iounit);
    }

    @Override
    protected void prepareSocket(InetSocketAddress socketAddress, boolean ssl) throws StyxException {
        try {
            mChanel = SocketChannel.open(socketAddress);
            mChanel.configureBlocking(true);
            Socket socket = mChanel.socket();
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
            final BufferImpl buffer = new BufferImpl(mIOUnit*2);
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
                                var message = deserializer.deserializeMessage(reader, mIOUnit);
                                if ( Checks.isTMessage(message.getType()) && ( mTMessageHandler != null )) {
                                        mTMessageHandler.postPacket(message, mServerClientDetails);
                                } else if ( mRMessageHandler != null ) {
                                    mRMessageHandler.postPacket(message, mServerClientDetails);
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
    public Collection<ClientDetails> getClients() {
        Set<ClientDetails> result = new HashSet<ClientDetails>();
        result.add(mServerClientDetails);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getClass().getSimpleName(),
                mChanel.socket().getLocalAddress().toString());
    }
}
