package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientChannelDriver extends TCPChannelDriver {
    public static final int PSEUDO_CLIENT_ID = 1;
    protected SocketChannel mChanel;
    protected ClientDetails mPseudoClientDetails;

    public TCPClientChannelDriver(InetAddress address, int port, boolean ssl, int IOUnit) throws IOException {
        super(address, port, ssl, IOUnit);
        mPseudoClientDetails = new TCPClientDetails(mChanel, this, IOUnit, PSEUDO_CLIENT_ID);
    }

    @Override
    protected void prepareSocket(InetSocketAddress socketAddress, boolean ssl) throws IOException {
        mChanel = SocketChannel.open(socketAddress);
        mChanel.configureBlocking(true);
        Socket socket = mChanel.socket();
        socket.setSoTimeout(getTimeout());
    }

    @Override
    public boolean isConnected() {
        return mChanel.isOpen();
    }

    @Override
    public boolean isStarted() {
        return isWorking;
    }

    @Override
    public boolean sendMessage(StyxMessage message, ClientDetails recepient) {
        if ( !recepient.equals(mPseudoClientDetails)) {
            throw new IllegalArgumentException("Wrong recepient");
        }
        return super.sendMessage(message, recepient);
    }

    @Override
    public void run() {
        try {
            isWorking = true;
            final StyxByteBufferReadable buffer = new StyxByteBufferReadable(mIOUnit*2);
            final StyxDataReader reader = new StyxDataReader(buffer);
            while (isWorking) {
                if (Thread.interrupted()) break;
                // read from socket
                try {
                    int readed = buffer.readFromChannel(mChanel);
                    if ( readed > 0 ) {
                        // loop unitl we have unprocessed packets in the input buffer
                        while ( buffer.remainsToRead() > 4 ) {
                            // try to decode
                            final long packetSize = reader.getUInt32();
                            if ( buffer.remainsToRead() >= packetSize ) {
                                final StyxMessage message = StyxMessage.factory(reader, mIOUnit);
                                if (mLogListener != null) {
                                    mLogListener.onMessageReceived(this, mPseudoClientDetails, message);
                                }
                                mMessageHandler.processPacket(message, mPseudoClientDetails);
                            } else {
                                break;
                            }
                        }
                    }
                }
                catch (SocketTimeoutException e) {
                    // Nothing to read
                    //                    e.printStackTrace();
                } catch (ClosedByInterruptException e) {
                    // finish
                    break;
                }
            }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            try {
                mChanel.close();
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
    public Set<ClientDetails> getClients() {
        Set<ClientDetails> result = new HashSet<ClientDetails>();
        result.add(mPseudoClientDetails);
        return result;
    }

    @Override
    public String toString() {
        try {
            return String.format("%s:%s", getClass().getSimpleName(), mChanel.getLocalAddress().toString());
        } catch (IOException e) {
            return e.toString();
        }
    }
}
