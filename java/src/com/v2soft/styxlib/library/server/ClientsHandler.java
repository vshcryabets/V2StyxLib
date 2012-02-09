package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
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
    implements Closeable {
    private int mIOUnit;
    private List<SocketChannel> mClients;
    private Map<SocketChannel, ClientState> mClientStatesMap;
    
    public ClientsHandler(int iounit) throws IOException {
        mIOUnit = iounit;
        mClients = new ArrayList<SocketChannel>();
        mClientStatesMap = new HashMap<SocketChannel, ClientState>();
    }

    public void addClient(SocketChannel client) throws IOException {
        client.configureBlocking(false);
        mClients.add(client);
        mClientStatesMap.put(client, new ClientState(mIOUnit));
    }

    @Override
    public void close() throws WebServiceException {
    }

	protected boolean readClient(SocketChannel channel) throws IOException {
		final ClientState state = mClientStatesMap.get(channel);
        int readed = channel.read(state.getBuffer());
        if ( readed == -1 ) {
        	removeClient(channel);
        	state.close();
        	return true;
        } else {
            state.process();
        }
        return false;
	}

	
	private void removeClient(SocketChannel channel) throws IOException {
    	mClientStatesMap.remove(channel);
    	channel.close();
	}
    
    
}
