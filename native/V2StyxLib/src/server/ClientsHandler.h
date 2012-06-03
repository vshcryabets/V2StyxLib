/*
 * ClientsHandler.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CLIENTSHANDLER_H_
#define CLIENTSHANDLER_H_
#include "../vfs/IVirtualStyxFile.h"
#include <vector>
#include <map>
#include "../types.h"
#include "ClientState.h"
#include <string>

typedef std::map<Socket, ClientState*> SocketsMap;
class ClientsHandler {
private:
	int mIOUnit;
	SocketsMap mClientStatesMap;
	IVirtualStyxFile *mRoot;
	std::string *mProtocol;

	void removeClient(Socket socket);
public:
	ClientsHandler(int iounit, IVirtualStyxFile *root, std::string *protocol);
	~ClientsHandler();
	void addClient(Socket socket);
	bool readClient(Socket socket);
};

#endif /* CLIENTSHANDLER_H_ */
