/*
 * ClientsHandler.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "ClientsHandler.h"
#include "ClientState.h"

ClientsHandler::ClientsHandler(int iounit,
		IVirtualStyxDirectory *root, std::string *protocol) : mIOUnit(iounit), mRoot(root), mProtocol(protocol) {
	mClientStatesMap = new std::map<Socket, ClientState*>();
}

ClientsHandler::~ClientsHandler() {
	// TODO delete ClientSTatet objects
	delete mClientStatesMap;
}

void ClientsHandler::addClient(Socket client) {
	mClientStatesMap->insert(
			std::pair<Socket, ClientState*>(client,
					new ClientState(mIOUnit, client, mRoot, mProtocol)));
}

bool ClientsHandler::readClient(Socket socket) {
	ClientState *state = mClientStatesMap->find(socket)->second;
	bool result = state->readSocket();
	if ( result ) {
		mClientStatesMap->erase(socket);
	}
	return result;
}
