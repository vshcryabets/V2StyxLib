package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Income connection handler class.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ConnectionAcceptor implements Runnable, Closeable {
    private boolean isWorking;
    private ServerSocketChannel mChannel;
    private ClientBalancer mBalancer;
    private Selector mSelector;

    public ConnectionAcceptor(ServerSocketChannel channel, ClientBalancer balancer) 
            throws IOException {
        mChannel = channel;
        mBalancer = balancer;
        mSelector = Selector.open();
        channel.configureBlocking(false);
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
                    mSelector.select();
                    Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                    while ( iterator.hasNext() ) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if ( key.isAcceptable() ) {
                            //                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            SocketChannel clientChannel = mChannel.accept();
                            clientChannel.configureBlocking(false);
                            clientChannel.register( mSelector, SelectionKey.OP_READ );
                            mBalancer.pushNewConnection(clientChannel);
                        } else if ( key.isReadable() ) {
                        	SocketChannel clientChannel = (SocketChannel) key.channel();
                        	mBalancer.pushReadable(clientChannel);
                        } else if ( key.isValid() ) {
                        	System.out.println("Key invalid");
                        }
                    }
                    mBalancer.process();
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
}
