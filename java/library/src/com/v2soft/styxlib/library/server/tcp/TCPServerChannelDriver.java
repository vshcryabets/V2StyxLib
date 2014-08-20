package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.server.ClientState;

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
import java.util.LinkedList;
import java.util.List;
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

    private ServerSocketChannel mChannel;
    private Selector mSelector;
    private Stack<SocketChannel> mNewConnetions, mReadable;
    protected HashSet<ClientState> mClients;
    private Map<SocketChannel, TCPClientState> mClientStatesMap;

    public TCPServerChannelDriver(InetAddress address, int port, boolean ssl, int IOUnit) throws IOException {
        super(address, port, ssl, IOUnit);
        mClients = new HashSet<ClientState>();
        mNewConnetions = new Stack<SocketChannel>();
        mReadable = new Stack<SocketChannel>();
        mClientStatesMap = new HashMap<SocketChannel, TCPClientState>();
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
    public void close() {
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
    }

    public void processEventsQueue() throws IOException {
        // new connections
        for (SocketChannel channel : mNewConnetions) {
            channel.configureBlocking(false);
            TCPClientState client = new TCPClientState(channel, this, mIOUnit);
            mMessageHandler.addClient(client);
            mClientStatesMap.put(channel, client);
            mClients.add(client);
        }
        mNewConnetions.clear();
        // new readables
        for (SocketChannel channel : mReadable) {
            final TCPClientState state = mClientStatesMap.get(channel);
            boolean result = readSocket(state);
            if ( result ) {
                removeClient(channel);
            }
        }
        mReadable.clear();
    }

    private void removeClient(SocketChannel channel) throws IOException {
        final ClientState client = mClientStatesMap.get(channel);
        mMessageHandler.removeClient(client);
        mClients.remove(client);
        mClientStatesMap.remove(channel);
        channel.close();
    }

    @Override
    public Set<ClientState> getClients() {
        return mClients;
    }
}
