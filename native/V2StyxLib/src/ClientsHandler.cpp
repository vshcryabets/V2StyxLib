/*
 * ClientsHandler.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "ClientsHandler.h"

ClientsHandler::ClientsHandler(int iounit, IVirtualStyxDirectory *root) :
mIOUnit(iounit), mRoot(root){
	mClients = new std::vector<Socket>();
	mClientStatesMap = new std::map<Socket, ClientState*>();
}

ClientsHandler::~ClientsHandler() {
	delete mClients;
	delete mClientStatesMap;
}

void ClientsHandler::addClient(Socket client) {
//	client.configureBlocking(false);
	mClients->push_back(client);
	mClientStatesMap->insert(
			std::pair<Socket, ClientState*>(client,
					new ClientState(mIOUnit, client, mRoot)));
}

/*	protected boolean readClient(SocketChannel channel) throws IOException {
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
	}*/
