/*
 * StyxServerManager.cpp
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "StyxServerManager.h"
#include "sys/socket.h"
#include "unistd.h"
#include "fcntl.h"
#include "string.h"
#include "netinet/in.h"
#include <arpa/inet.h>
#include <netdb.h>

StyxServerManager::StyxServerManager(string address,
		int port,
		IVirtualStyxDirectory *root):mPort(port), mRoot(root) {
	// resolve address
	mSocket = socket(AF_INET, SOCK_STREAM, 0);
	if ( mSocket == -1 ) {
		throw "Can't create socket socket()";
	}
	int reuse_addr = 1;
	setsockopt(mSocket, SOL_SOCKET, SO_REUSEADDR, &reuse_addr,
			sizeof(reuse_addr));
	// non-blocking mode
	int opts;

	opts = fcntl(mSocket,F_GETFL);
	if (opts < 0) {
		throw "Can't create socket fcntl(F_GETFL)";
	}
	opts = (opts | O_NONBLOCK);
	if (fcntl(mSocket,F_SETFL,opts) < 0) {
		throw "Can't create socket fcntl(F_SETFL)";
	}
	// bind socket
	struct sockaddr_in server_address;
	this->setAddress(address.c_str(), port, &server_address, NULL);
	if (bind(mSocket, (struct sockaddr *) &server_address,
			sizeof(server_address)) < 0 ) {
		close(mSocket);
		throw "Can't create socket bind()";
	}
}

StyxServerManager::~StyxServerManager() {
	// TODO Auto-generated destructor stub
}

void StyxServerManager::setAddress(const char * hname,
		short port,
		struct sockaddr_in * sap,
		char * protocol) {
	struct hostent *hp;

	memset(sap, 0, sizeof(*sap));
	sap->sin_family = AF_INET;
	if(hname != NULL) {
		if(!inet_aton(hname, &sap->sin_addr)) {
			hp = gethostbyname(hname);
			if(hp == NULL)
				throw "unknown host";
			sap->sin_addr = *(struct in_addr *)hp->h_addr;
		}
	}
	else {
		sap->sin_addr.s_addr = htonl( INADDR_ANY);
	}
	sap->sin_port = htons(port);
}

void StyxServerManager::start() {

}

void StyxServerManager::stop() {

}

