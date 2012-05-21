/*
 * StyxServerManager.h
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef STYXSERVERMANAGER_H_
#define STYXSERVERMANAGER_H_
#include "IVirtualStyxDirectory.h"
#include <string>
/* According to POSIX.1-2001 */
#include <sys/select.h>
using namespace std;

class StyxServerManager {
private:
	int mSocket;
	int mPort;
	IVirtualStyxDirectory *mRoot;

	void setAddress(const char * hname,
			short port,
			struct sockaddr_in * sap,
			char * protocol);
	void readSockets(fd_set* socks, int *list);
	void handleNewConnection(int* connectlist);
	void deaWithData(int* list, int id);
	void setNonBlocking(int socket);
public:
	StyxServerManager(string address, int port, IVirtualStyxDirectory *root);
	~StyxServerManager();
	/**
	 * start server
	 */
	void start();
	/**
	 * stop server and realease all resources
	 */
	void stop();
};

#endif /* STYXSERVERMANAGER_H_ */
