package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.io.StyxByteBufferReadable;
import com.v2soft.styxlib.io.StyxDataReader;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.utils.SyncObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientChannelDriver extends TCPChannelDriver {
    public static final int PSEUDO_CLIENT_ID = 1;
    protected TCPClientDetails mServerClientDetails;
    protected SocketChannel mSocket;

    public TCPClientChannelDriver(String address, int port) throws IOException {
        super(address, port);
    }

    @Override
    public void prepareSocket() throws StyxException {
        InetSocketAddress socketAddress = new InetSocketAddress(mAddress, mPort);
        try {
            mSocket = SocketChannel.open(socketAddress);
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_CREATE_ERROR);
        }
        try {
            mSocket.configureBlocking(true);
            mSocket.socket().setSoTimeout(getTimeout());
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_CONFIGURE_ERROR);
        }
        mServerClientDetails = new TCPClientDetails(mSocket, this, mIOUnit, PSEUDO_CLIENT_ID);
    }

    @Override
    public void closeSocket() throws StyxException {
        try {
            mSocket.close();
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_CLOSE_ERROR);
        }
    }

    @Override
    public boolean isConnected() {
        if ( mServerClientDetails.getChannel() == null ) {
            return false;
        }
        return mServerClientDetails.getChannel().isOpen();
    }

    @Override
    public boolean isStarted() {
        return isWorking;
    }

    @Override
    public boolean sendMessage(StyxMessage message, ClientDetails recipient) {
        if ( !recipient.equals(mServerClientDetails)) {
            throw new IllegalArgumentException("Wrong recipient");
        }
        return super.sendMessage(message, recipient);
    }

    @Override
    public StyxMessage sendMessageAndWaitAnswer(StyxMessage answer, ClientDetails recepient, SyncObject syncObject)
            throws IOException, InterruptedException, StyxErrorMessageException, TimeoutException {
        return null;
    }

    @Override
    public void run() {
        try {
            isWorking = true;
            // TODO we can use buffer and reader from mServerClientDetails
            final StyxByteBufferReadable buffer = new StyxByteBufferReadable(mIOUnit*2);
            final StyxDataReader reader = new StyxDataReader(buffer);
            while (isWorking) {
                if (Thread.interrupted()) break;
                // read from socket
                try {
                    int readed = buffer.readFromChannel(mServerClientDetails.getChannel());
                    if ( readed > 0 ) {
                        // loop unitl we have unprocessed packets in the input buffer
                        while ( buffer.remainsToRead() > 4 ) {
                            // try to decode
                            final long packetSize = reader.getUInt32();
                            if ( buffer.remainsToRead() >= packetSize ) {
                                final StyxMessage message = StyxMessage.factory(reader, mIOUnit);
                                if (mLogListener != null) {
                                    mLogListener.onMessageReceived(this, mServerClientDetails, message);
                                }
                                if ( message.getType().isTMessage() ) {
                                    mTMessageHandler.postPacket(message, mServerClientDetails);
                                } else {
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
        mThread.interrupt();
    }

    @Override
    public Collection<ClientDetails> getClients() {
        Set<ClientDetails> result = new HashSet<ClientDetails>();
        result.add(mServerClientDetails);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s_%s:%d", getClass().getSimpleName(),
                mAddress, mPort);
    }
}
