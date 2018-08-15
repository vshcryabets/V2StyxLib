package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPServerChannelDriver extends TCPChannelDriver {
    protected ServerSocketChannel mChannel;
    protected Stack<SocketChannel> mNewConnetions, mReadable;
    protected Map<SocketChannel, ClientDetails> mClientStatesMap;
    protected int mLastClientId = 1;

    public TCPServerChannelDriver(String address, int port) {
        super(address, port);
        mNewConnetions = new Stack<>();
        mReadable = new Stack<>();
        mClientStatesMap = new HashMap<>();
    }

    @Override
    public void prepareSocket() throws StyxException {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(mAddress);
        } catch (UnknownHostException e) {
            throw new StyxException(StyxException.DRIVER_CANT_RESOLVE_NAME);
        }
        InetSocketAddress socketAddress = new InetSocketAddress(address, mPort);
        try {
            mChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_CREATE_ERROR);
        }
        ServerSocket socket = mChannel.socket();
        try {
            socket.bind(socketAddress);
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_BIND_ERROR, String.format(
                    "Can't bind socket in TCPServerChannelDriver::prepareSocket() %s",
                    e.getMessage()));
        }
        try {
            socket.setReuseAddress(true);
            socket.setSoTimeout(getTimeout());
            mChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_CONFIGURE_ERROR);
        }
    }

    @Override
    public boolean isStarted() {
        return isWorking;
    }

    @Override
    public void run() {
        isWorking = true;
        Selector selector = null;
        try {
            selector = Selector.open();
            mChannel.register(selector, SelectionKey.OP_ACCEPT);

            while ( isWorking ) {
                try {
                    if ( !selector.isOpen() ) {
                        break;
                    }
                    selector.select();
                    if ( !selector.isOpen() ) {
                        break;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while ( iterator.hasNext() ) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if ( key.isAcceptable() ) {
                            SocketChannel clientChannel = mChannel.accept();
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ);
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
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                closeSocket();
            } catch (StyxException e) {
                e.printStackTrace();
            }
        }
        mChannel = null;
        isWorking = false;
    }

    private void setNonBlocking(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
    }

    public void closeSocket() throws StyxException {
        if (mChannel == null) {
            return;
        }
        try {
            mChannel.close();
            mChannel = null;
        } catch (IOException e) {
            throw new StyxException(StyxException.DRIVER_CLOSE_ERROR);
        }
    }

    protected void processEventsQueue() throws IOException {
        // new connections
        for (SocketChannel channel : mNewConnetions) {
            setNonBlocking(channel);
            TCPClientDetails client = new TCPClientDetails(channel, this,
                    mIOUnit, mLastClientId++);
            mRMessageHandler.addClient(client);
            mTMessageHandler.addClient(client);
            mClientStatesMap.put(channel, client);
        }
        mNewConnetions.clear();
        // new readables
        for (SocketChannel channel : mReadable) {
            final TCPClientDetails details = (TCPClientDetails) mClientStatesMap.get(channel);
            if ( readSocket(details) ) {
                mTMessageHandler.removeClient(details);
                mRMessageHandler.removeClient(details);
                mClientStatesMap.remove(channel);
                channel.close();
            }
        }
        mReadable.clear();
    }

    @Override
    public Collection<ClientDetails> getClients() {
        return mClientStatesMap.values();
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
