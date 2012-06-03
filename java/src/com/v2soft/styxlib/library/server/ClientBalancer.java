package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;


public class ClientBalancer {
//    private String mProtocol;
    private ClientsHandler mHandler;
    private Thread mThread;
    private List<SocketChannel> mNewConnetions, mReadable;
    
    public ClientBalancer(int iounit, IVirtualStyxFile root, String protocol) throws IOException {
    	mNewConnetions = new ArrayList<SocketChannel>();
    	mReadable = new ArrayList<SocketChannel>();
        mHandler = new ClientsHandler(iounit, root, protocol);
//        mThread = new Thread(mHandler, "ClientsHandler");
//        mThread.start();
    }
    
    public void pushNewConnection(SocketChannel client) throws IOException {
    	mNewConnetions.add(client);
    }

	public void process() throws IOException {
		// new connections
		for (SocketChannel channel : mNewConnetions) {
			mHandler.addClient(channel);
		}
		mNewConnetions.clear();
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
