package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;
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
    protected Stack<SocketChannel> mReadable;
    protected Map<SocketChannel, Integer> mClientStatesMap;

    public TCPServerChannelDriver(InetAddress address,
                                  int port,
                                  boolean ssl,
                                  ClientsRepo clientsRepo) throws StyxException {
        super(address, port, ssl, clientsRepo);
        mReadable = new Stack<>();
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
                Stack<SocketChannel> newConnetions = new Stack<>();
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
                            newConnetions.push(clientChannel);
                        } else if ( key.isReadable() ) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            mReadable.push(clientChannel);
                        } else if ( key.isValid() ) {
                            System.out.println("Key invalid");
                        }
                    }
                    processEventsQueue(newConnetions);
                    newConnetions.clear();
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

    protected void processEventsQueue(Stack<SocketChannel> newConnetions) throws StyxException {
        // new connections
        for (SocketChannel channel : newConnetions) {
            try {
                channel.configureBlocking(false);
            } catch (IOException error) {
                throw new StyxException(error.getMessage());
            }
            int id = mClientsRepo.addClient(new TCPClientDetails(channel, this, mIOUnit));
            mRMessageHandler.addClient(id);
            mTMessageHandler.addClient(id);
            mClientStatesMap.put(channel, id);
        }
        // new readables
        for (SocketChannel channel : mReadable) {
            final int clientId = mClientStatesMap.get(channel);
            boolean result = readSocket(clientId);
            if ( result ) {
                removeClient(channel);
            }
        }
        mReadable.clear();
    }

    private void removeClient(SocketChannel channel) throws StyxException {
        var cleintId = mClientStatesMap.get(channel);
        mTMessageHandler.removeClient(cleintId);
        mRMessageHandler.removeClient(cleintId);
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
