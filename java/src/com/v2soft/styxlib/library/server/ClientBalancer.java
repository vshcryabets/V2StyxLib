package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.v2soft.styxlib.library.server.vfs.IVirtualStyxDirectory;


public class ClientBalancer {
    private ClientsHandler mHandler;
    private Thread mThread;
    private List<SocketChannel> mNewConnetion, mReadable;
    
    public ClientBalancer(int iounit, IVirtualStyxDirectory root) throws IOException {
    	mNewConnetion = new ArrayList<SocketChannel>();
    	mReadable = new ArrayList<SocketChannel>();
        mHandler = new ClientsHandler(iounit, root);
//        mThread = new Thread(mHandler, "ClientsHandler");
//        mThread.start();
    }
    
    public void pushNewConnection(SocketChannel client) throws IOException {
    	mNewConnetion.add(client);
    }

	public void process() throws IOException {
		// new connections
		for (SocketChannel channel : mNewConnetion) {
			mHandler.addClient(channel);
		}
		mNewConnetion.clear();
		// new readables
		for (SocketChannel channel : mReadable) {
			boolean closed = mHandler.readClient(channel);
		}
		mReadable.clear();
	}

	public void pushReadable(SocketChannel clientChannel) {
		mReadable.add(clientChannel);
	}
}
