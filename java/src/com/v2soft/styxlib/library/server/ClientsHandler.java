package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.Closeable;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientsHandler 
    implements Runnable, Closeable {
    private int mIOUnit;
    private boolean isWorking;
    private List<SocketChannel> mClients;
    private Selector mSelector;
    private java.nio.ByteBuffer mBuffer;
    private Map<SocketChannel, ClientState> mClientStatesMap;
    
    public ClientsHandler(int iounit) throws IOException {
        mIOUnit = iounit;
        mSelector = Selector.open();
        mClients = new ArrayList<SocketChannel>();
        mBuffer = java.nio.ByteBuffer.allocateDirect(iounit);
        mClientStatesMap = new HashMap<SocketChannel, ClientState>();
    }

    public void addClient(SocketChannel client) throws IOException {
        client.configureBlocking(false);
        mSelector.wakeup();
        client.register(mSelector, SelectionKey.OP_READ);
        mClients.add(client);
        mClientStatesMap.put(client, new ClientState(mIOUnit));
    }
    
    @Override
    public void run() {
        isWorking = true;
        while (isWorking) {
            try {
                mSelector.select();
                Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                while ( iterator.hasNext() ) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if ( key.isAcceptable() ) {
                        System.out.println(
                                " Before: Remaining="+mBuffer.remaining()+
                                " Capacity="+mBuffer.capacity()+
                                " Limit="+mBuffer.limit()+
                                " Position="+mBuffer.position()
                                );
                        SocketChannel channel = (SocketChannel) key.channel();
                        int readed = channel.read(mBuffer);
                        System.out.println(
                                " After: Remaining="+mBuffer.remaining()+
                                " Capacity="+mBuffer.capacity()+
                                " Limit="+mBuffer.limit()+
                                " Position="+mBuffer.position()
                                );
                        
                        mClientStatesMap.get(channel).process(mBuffer, readed);
                        mBuffer.clear();
                    }
                }
            } catch (IOException e) {
                // this is ok
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    public void close() throws WebServiceException {
        try {
            mSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
