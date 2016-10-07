/*
 * ClientsHandler.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "server/ClientsHandler.h"
#include "server/ClientState.h"

ClientsHandler::ClientsHandler(int iounit,
		IVirtualStyxFile *root, std::string protocol) : mIOUnit(iounit), mRoot(root), mProtocol(protocol) {
}

ClientsHandler::~ClientsHandler() {
}

void ClientsHandler::addClient(Socket client) {
	mClientStatesMap.insert(
			std::pair<Socket, ClientDetails*>(client,
					new ClientDetails(mIOUnit, client, mRoot, mProtocol)));
}

bool ClientsHandler::readClient(Socket socket) {
	SocketsMap::iterator it = mClientStatesMap.find(socket);
	if ( it != mClientStatesMap.end() ) {
		bool result = it->second->readSocket();
		if ( result ) {
			mClientStatesMap.erase(it);
		}
		return result;
	}
	return true;
}
