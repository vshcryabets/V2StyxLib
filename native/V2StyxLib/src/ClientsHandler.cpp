/*
 * ClientsHandler.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "ClientsHandler.h"
#include "ClientState.h"

ClientsHandler::ClientsHandler(int iounit,
		IVirtualStyxDirectory *root) : mIOUnit(iounit), mRoot(root) {
	mClientStatesMap = new std::map<Socket, ClientState*>();
}

ClientsHandler::~ClientsHandler() {
	delete mClientStatesMap;
}

void ClientsHandler::addClient(Socket client) {
	mClientStatesMap->insert(
			std::pair<Socket, ClientState*>(client,
					new ClientState(mIOUnit, client, mRoot)));
}

bool ClientsHandler::readClient(Socket socket) {
	ClientState *state = mClientStatesMap->find(socket)->second;
	bool result = state->read();
	if ( result ) {
		removeClient(socket);
	}
	return result;
}

void ClientsHandler::removeClient(Socket socket) {
	mClientStatesMap->erase(socket);
	close(socket);
}
