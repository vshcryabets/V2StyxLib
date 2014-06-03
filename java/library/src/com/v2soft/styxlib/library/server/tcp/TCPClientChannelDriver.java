package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IClientChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientChannelDriver extends TCPChannelDriver implements IClientChannelDriver {
    protected SocketChannel mChanel;
    protected ClientState mPseudoClient;

    public TCPClientChannelDriver(InetAddress address, int port, boolean ssl, int IOUnit) throws IOException {
        super(address, port, ssl, IOUnit);
        mPseudoClient = new TCPClientState(mChanel, this, IOUnit);
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
    public boolean sendMessage(StyxMessage message) {
        super.sendMessage(mPseudoClient, message);
        return true;
    }

    @Override
    public void run() {
        try {
            final StyxByteBufferReadable buffer = new StyxByteBufferReadable(mIOUnit*2);
            final StyxDataReader reader = new StyxDataReader(buffer);
            isWorking = true;
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
                                mMessageHandler.processPacket(mPseudoClient, message);
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
        }
    }

    public Socket getSocket() {
        return mChanel.socket();
    }
}
