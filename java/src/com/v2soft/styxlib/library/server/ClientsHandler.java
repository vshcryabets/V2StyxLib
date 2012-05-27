package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxDirectory;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientsHandler 
    implements Closeable {
    private String mProtocol;
    private int mIOUnit;
    private Map<SocketChannel, ClientState> mClientStatesMap;
    private IVirtualStyxDirectory mRoot;
    
    public ClientsHandler(int iounit, IVirtualStyxDirectory root, String protocol) throws IOException {
        mIOUnit = iounit;
        mClientStatesMap = new HashMap<SocketChannel, ClientState>();
        mRoot = root;
        mProtocol = protocol;
    }

    public void addClient(SocketChannel client) throws IOException {
        client.configureBlocking(false);
        mClientStatesMap.put(client, new ClientState(mIOUnit, client, mRoot, mProtocol));
    }

    @Override
    public void close() throws IOException {
    }

	protected boolean readClient(SocketChannel channel) throws IOException {
		final ClientState state = mClientStatesMap.get(channel);
		boolean result = state.readSocket();
		if ( result ) {
		    removeClient(channel);
		}
		return result;
	}

	
	private void removeClient(SocketChannel channel) throws IOException {
	    final ClientState state = mClientStatesMap.get(channel);
	    mRoot.onConnectionClosed(state);
    	mClientStatesMap.remove(channel);
    	channel.close();
	}
    
    
}
