/*
 * ClientsHandler.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CLIENTSHANDLER_H_
#define CLIENTSHANDLER_H_
#include "IVirtualStyxDirectory.h"
#include <vector>
#include <map>
#include "types.h"
#include "ClientState.h"

class ClientsHandler {
private:
	int mIOUnit;
	std::vector<Socket> *mClients;
	std::map<Socket, ClientState*> *mClientStatesMap;
	IVirtualStyxDirectory *mRoot;
public:
	ClientsHandler(int iounit, IVirtualStyxDirectory *root);
	~ClientsHandler();
	void addClient(Socket socket);
};

#endif /* CLIENTSHANDLER_H_ */
