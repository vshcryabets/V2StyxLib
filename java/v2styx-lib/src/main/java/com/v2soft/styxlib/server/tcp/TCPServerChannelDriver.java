package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientsRepo;

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
    protected ServerSocketChannel mChannel;
    protected Selector mSelector;
    protected Map<SocketChannel, Integer> mClientStatesMap;

    public TCPServerChannelDriver(InetAddress address,
                                  int port,
                                  boolean ssl,
                                  ClientsRepo clientsRepo) throws StyxException {
        super(address, port, ssl, clientsRepo);
        mClientStatesMap = new HashMap<>();
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
                final var newConnections = new ArrayList<SocketChannel>();
                final var readable = new ArrayList<SocketChannel>();
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
                            newConnections.add(clientChannel);
                        } else if ( key.isReadable() ) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            readable.add(clientChannel);
                        } else if ( key.isValid() ) {
                            System.out.println("Key invalid");
                        }
                    }
                    processEventsQueue(newConnections, readable);
                    newConnections.clear();
                    readable.clear();
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

    protected void processEventsQueue(List<SocketChannel> newConnections,
                                      List<SocketChannel> readable) throws StyxException {
        // new connections
        for (SocketChannel channel : newConnections) {
            try {
                channel.configureBlocking(false);
            } catch (IOException error) {
                throw new StyxException(error.getMessage());
            }
            int id = mClientsRepo.addClient(new TCPClientDetails(channel, this, mIOUnit));
            mClientStatesMap.put(channel, id);
        }
        // new readables
        for (SocketChannel channel : readable) {
            final int clientId = mClientStatesMap.get(channel);
            boolean result = readSocket(clientId);
            if ( result ) {
                removeClient(channel);
            }
        }
    }

    private void removeClient(SocketChannel channel) throws StyxException {
        var cleintId = mClientStatesMap.get(channel);
        mTMessageHandler.onClientRemoved(cleintId);
        mRMessageHandler.onClientRemoved(cleintId);
        mClientsRepo.removeClient(cleintId);
        mClientStatesMap.remove(channel);
        try {
            channel.close();
        } catch (IOException error) {
            throw new StyxException(error.getMessage());
        }
    }

    @Override
    public Collection<Integer> getClients() {
        return mClientStatesMap.values();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void clearStatistics() {

    }
}
