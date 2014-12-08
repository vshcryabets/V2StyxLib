package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
    protected HashSet<ClientDetails> mClientDetailses;
    protected Map<SocketChannel, TCPClientDetails> mClientStatesMap;
    protected int mLastClientId = 1;

    public TCPServerChannelDriver(InetAddress address, int port, boolean ssl) throws IOException {
        super(address, port, ssl);
        mClientDetailses = new HashSet<ClientDetails>();
        mNewConnetions = new Stack<SocketChannel>();
        mReadable = new Stack<SocketChannel>();
        mClientStatesMap = new HashMap<SocketChannel, TCPClientDetails>();
    }

    protected void prepareSocket(InetSocketAddress isa, boolean useSSL) throws IOException {
        if (useSSL) {
            throw new RuntimeException("Not implemented");
        } else {
            mChannel = ServerSocketChannel.open();
        }
        ServerSocket socket = mChannel.socket();
        socket.bind(isa);
        socket.setReuseAddress(true);
        socket.setSoTimeout(getTimeout());
        mSelector = Selector.open();
        mChannel.configureBlocking(false);
    }

    @Override
    public boolean isStarted() {
        return isWorking;
    }

    @Override
    public void close() {
        super.close();
        try {
            mSelector.close();
            mChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        }
        isWorking = false;
    }

    protected void processEventsQueue() throws IOException {
        // new connections
        for (SocketChannel channel : mNewConnetions) {
            channel.configureBlocking(false);
            TCPClientDetails client = new TCPClientDetails(channel, this, mIOUnit, mLastClientId++);
            mRMessageHandler.addClient(client);
            mTMessageHandler.addClient(client);
            mClientStatesMap.put(channel, client);
            mClientDetailses.add(client);
        }
        mNewConnetions.clear();
        // new readables
        for (SocketChannel channel : mReadable) {
            final TCPClientDetails state = mClientStatesMap.get(channel);
            boolean result = readSocket(state);
            if ( result ) {
                removeClient(channel);
            }
        }
        mReadable.clear();
    }

    private void removeClient(SocketChannel channel) throws IOException {
        final ClientDetails clientDetails = mClientStatesMap.get(channel);
        mTMessageHandler.removeClient(clientDetails);
        mRMessageHandler.removeClient(clientDetails);
        mClientDetailses.remove(clientDetails);
        mClientStatesMap.remove(channel);
        channel.close();
    }

    @Override
    public Set<ClientDetails> getClients() {
        return mClientDetailses;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
