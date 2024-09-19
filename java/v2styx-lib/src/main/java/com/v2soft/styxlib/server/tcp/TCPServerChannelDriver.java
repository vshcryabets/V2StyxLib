package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPServerChannelDriver extends TCPChannelDriver {
    private static final String TAG = TCPServerChannelDriver.class.getSimpleName();

    protected ServerSocketChannel mChannel;
    protected Selector mSelector;
    protected Stack<SocketChannel> mNewConnetions, mReadable;
    protected Map<SocketChannel, ClientDetails> mClientStatesMap;
    protected int mLastClientId = 1;

    public TCPServerChannelDriver(InetAddress address, int port, boolean ssl) throws StyxException {
        super(address, port, ssl);
        mNewConnetions = new Stack<SocketChannel>();
        mReadable = new Stack<SocketChannel>();
        mClientStatesMap = new HashMap<SocketChannel, ClientDetails>();
    }

    protected void prepareSocket(InetSocketAddress isa, boolean useSSL) throws StyxException {
        if (useSSL) {
            throw new StyxException("Not implemented");
        } else {
            try {
                mChannel = ServerSocketChannel.open();
            } catch (IOException e) {
                throw new StyxException(e.getMessage());
            }
        }
        try {
            ServerSocket socket = mChannel.socket();
            socket.bind(isa);
            socket.setReuseAddress(true);
            socket.setSoTimeout(getTimeout());
            mSelector = Selector.open();
            mChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new StyxException(e.getMessage());
        }

    }

    @Override
    public boolean isStarted() {
        return isWorking;
    }

    @Override
    public void run() {
        isWorking = true;
        try {
            mChannel.register(mSelector, SelectionKey.OP_ACCEPT);

            while ( isWorking ) {
                try {
                    if ( !mSelector.isOpen() ) {
                        break;
                    }
                    mSelector.select();
                    if ( !mSelector.isOpen() ) {
                        break;
                    }
                    Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                    while ( iterator.hasNext() ) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if ( key.isAcceptable() ) {
                            SocketChannel clientChannel = mChannel.accept();
                            clientChannel.configureBlocking(false);
                            clientChannel.register(mSelector, SelectionKey.OP_READ);
                            mNewConnetions.push(clientChannel);
                        } else if ( key.isReadable() ) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            mReadable.push(clientChannel);
                        } else if ( key.isValid() ) {
                            System.out.println("Key invalid");
                        }
                    }
                    processEventsQueue();
                } catch (IOException e) {
                    // this is ok
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mChannel = null;
        isWorking = false;
    }

    protected void processEventsQueue() throws StyxException {
        // new connections
        for (SocketChannel channel : mNewConnetions) {
            try {
                channel.configureBlocking(false);
            } catch (IOException error) {
                throw new StyxException(error.getMessage());
            }
            TCPClientDetails client = new TCPClientDetails(channel, this, mIOUnit, mLastClientId++);
            mRMessageHandler.addClient(client);
            mTMessageHandler.addClient(client);
            mClientStatesMap.put(channel, client);
        }
        mNewConnetions.clear();
        // new readables
        for (SocketChannel channel : mReadable) {
            final TCPClientDetails state = (TCPClientDetails) mClientStatesMap.get(channel);
            boolean result = readSocket(state);
            if ( result ) {
                removeClient(channel);
            }
        }
        mReadable.clear();
    }

    private void removeClient(SocketChannel channel) throws StyxException {
        final ClientDetails clientDetails = mClientStatesMap.get(channel);
        mTMessageHandler.removeClient(clientDetails);
        mRMessageHandler.removeClient(clientDetails);
        mClientStatesMap.remove(channel);
        try {
            channel.close();
        } catch (IOException error) {
            throw new StyxException(error.getMessage());
        }
    }

    @Override
    public Collection<ClientDetails> getClients() {
        return mClientStatesMap.values();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void clearStatisitcis() {

    }
}
