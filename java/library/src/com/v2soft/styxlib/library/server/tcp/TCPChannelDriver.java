package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.io.StyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientBalancer;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPChannelDriver implements IChannelDriver, Runnable {
    private static final String TAG = TCPChannelDriver.class.getSimpleName();
    private int mPort;
    private boolean mSSL;
    private boolean mNeedAuth;
    private Thread mAcceptorThread;
    private boolean isWorking;
    private ServerSocketChannel mChannel;
    private ClientBalancer mBalancer;
    private Selector mSelector;
    private Stack<SocketChannel> mNewConnetions, mReadable;
    private Map<SocketChannel, TCPClientState> mClientStatesMap;
    protected int mIOUnit;

    public TCPChannelDriver(InetAddress address, int port, boolean ssl, int IOUnit) throws IOException {
        mPort = port;
        ServerSocketChannel channel = null;
        if (ssl) {
            throw new RuntimeException("Not implemented");
        } else {
            channel = ServerSocketChannel.open();
        }

        mIOUnit = IOUnit;

        // Bind the server socket to the local host and port
        InetSocketAddress isa = new InetSocketAddress(address, port);
        ServerSocket socket = channel.socket();
        socket.bind(isa);
        socket.setReuseAddress(true);
        socket.setSoTimeout(getTimeout());

        mChannel = channel;
        mSelector = Selector.open();
        channel.configureBlocking(false);

        mNewConnetions = new Stack<SocketChannel>();
        mReadable = new Stack<SocketChannel>();
        mClientStatesMap = new HashMap<SocketChannel, TCPClientState>();
    }

    private int getTimeout() {
        return StyxServerManager.DEFAULT_TIMEOUT;
    }

    @Override
    public Thread start() {
        if ( mBalancer == null ) {
            throw new IllegalStateException("Balancer is null");
        }
        mAcceptorThread = new Thread(this, TAG);
        mAcceptorThread.start();
        return mAcceptorThread;
    }

    @Override
    public void sendMessage(ClientState client, StyxMessage answer) {
        ByteBuffer buffer = ((TCPClientState)client).getOutputBuffer();
        try {
            answer.writeToBuffer(new StyxDataWriter(buffer));
            buffer.position(0);
            ((TCPClientState)client).getChannel().write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBalancer(ClientBalancer balancer) {
        mBalancer = balancer;
    }

    @Override
    public void close() {
        isWorking= false;
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
            mBalancer.addClient(client);
            mClientStatesMap.put(channel, client);
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
        mBalancer.removeClient(client);
        mClientStatesMap.remove(channel);
        channel.close();
    }


    /**
     * Read data from assigned SocketChannel
     * @return
     * @throws IOException
     */
    public boolean readSocket(TCPClientState client) throws IOException {
        int read = 0;
        try {
            read = client.getInputBuffer().readFromChannel(client.getChannel());
        }
        catch (IOException e) {
            read = -1;
        }
        if ( read == -1 ) {
            close();
            return true;
        } else {
            while ( process(client) );
        }
        return false;
    }

    /**
     *
     * @return true if message was processed
     * @throws IOException
     */
    private boolean process(TCPClientState client) throws IOException {
        int inBuffer = client.getInputBuffer().remainsToRead();
        if ( inBuffer > 4 ) {
            long packetSize = client.getInputReader().getUInt32();
            if ( inBuffer >= packetSize ) {
                final StyxMessage message = StyxMessage.factory(client.getInputReader(), mIOUnit);
                mBalancer.processPacket(client, message);
                return true;
            }
        }
        return false;
    }

}
