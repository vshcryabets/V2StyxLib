/*
 * StyxServerManager.h
 *
 *  Created on: May 20, 2012
 *      Author: mrco
 */

#ifndef STYXSERVERMANAGER_H_
#define STYXSERVERMANAGER_H_
#include "IVirtualStyxDirectory.h"
#include <string>
using namespace std;

class StyxServerManager {
private:
	int mPort;
	IVirtualStyxDirectory *mRoot;
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
	void close();
};

#endif /* STYXSERVERMANAGER_H_ */
