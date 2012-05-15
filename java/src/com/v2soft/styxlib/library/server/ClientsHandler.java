package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import com.v2soft.styxlib.library.server.vfs.IVirtualStyxDirectory;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientsHandler 
    implements Closeable {
    private int mIOUnit;
    private List<SocketChannel> mClients;
    private Map<SocketChannel, ClientState> mClientStatesMap;
    private IVirtualStyxDirectory mRoot;
    
    public ClientsHandler(int iounit, IVirtualStyxDirectory root) throws IOException {
        mIOUnit = iounit;
        mClients = new ArrayList<SocketChannel>();
        mClientStatesMap = new HashMap<SocketChannel, ClientState>();
        mRoot = root;
    }

    public void addClient(SocketChannel client) throws IOException {
        client.configureBlocking(false);
        mClients.add(client);
        mClientStatesMap.put(client, new ClientState(mIOUnit, client, mRoot));
    }

    @Override
    public void close() throws WebServiceException {
    }

	protected boolean readClient(SocketChannel channel) throws IOException {
		final ClientState state = mClientStatesMap.get(channel);
		boolean result = state.read();
		if ( result ) {
		    removeClient(channel);
		}
		return result;
	}

	
	private void removeClient(SocketChannel channel) throws IOException {
    	mClientStatesMap.remove(channel);
    	channel.close();
	}
    
    
}
