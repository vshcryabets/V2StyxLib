/*
 * ClientsHandler.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CLIENTSHANDLER_H_
#define CLIENTSHANDLER_H_
#include "../vfs/IVirtualStyxDirectory.h"
#include <vector>
#include <map>
#include "../types.h"
#include "ClientState.h"
#include <string>

class ClientsHandler {
private:
	int mIOUnit;
	std::map<Socket, ClientState*> *mClientStatesMap;
	IVirtualStyxDirectory *mRoot;
	std::string *mProtocol;

	void removeClient(Socket socket);
public:
	ClientsHandler(int iounit, IVirtualStyxDirectory *root, std::string *protocol);
	~ClientsHandler();
	void addClient(Socket socket);
	bool readClient(Socket socket);
};

#endif /* CLIENTSHANDLER_H_ */
