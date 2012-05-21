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
