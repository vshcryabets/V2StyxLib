package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;


public class ClientBalancer {
    private ClientsHandler mHandler;
    private Thread mThread;
    
    public ClientBalancer(int iounit) throws IOException {
        mHandler = new ClientsHandler(iounit);
        mThread = new Thread(mHandler, "ClientsHandler");
        mThread.start();
    }
    
    public void push(SocketChannel client) throws IOException {
        mHandler.addClient(client);
    }
}
