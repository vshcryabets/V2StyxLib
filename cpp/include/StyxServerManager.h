/*
 * StyxServerManager.h
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef STYXSERVERMANAGER_H_
#define STYXSERVERMANAGER_H_
#include <string>
#ifdef WIN32
#include <WinSock2.h>
#else
/* According to POSIX.1-2001 */
#include <sys/select.h>
#endif

#include "types.h"
#include "classes.h"
using namespace std;

class StyxServerManager {
private:
	ClientBalancer *mBalancer;
	ConnectionAcceptor *mAcceptor;
	Socket mSocket;
	int mPort;
	int mIOBufSize;
	IVirtualStyxFile *mRoot;

	void setAddress(const char * hname,
			short port,
			struct sockaddr_in * sap,
			char* protocol);
	// create and bind socket
	Socket createSocket(string address, int port);
public:
	StyxServerManager(string address, int port, IVirtualStyxFile *root, string protocol);
	~StyxServerManager();
	/**
	 * start server
	 */
	void start();
	/**
	 * stop server and realease all resources
	 */
	void stop();
	/**
	* Set non-blocking mode
	*/
	static void setNonBlocking(Socket socket);
};

#endif /* STYXSERVERMANAGER_H_ */
