/*
 * StyxServerManager.cpp
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "StyxServerManager.h"
#ifdef WIN32
#include <WinSock2.h>
#else
#include "sys/socket.h"
#include "netinet/in.h"
#include <arpa/inet.h>
#include <netdb.h>
#endif
#include "fcntl.h"
#include "string.h"
#include <stdio.h>
#include "server/ClientBalancer.h"
#include "server/ConnectionAcceptor.h"
#include "StyxLibraryException.h"


StyxServerManager::StyxServerManager(string address,
		int port,
		IVirtualStyxFile *root, std::string protocol)
	:mPort(port),  mIOBufSize(8192), mRoot(root) {
	mSocket = createSocket(address, port);
    mBalancer = new ClientBalancer(mIOBufSize, root, protocol);
    mAcceptor = new ConnectionAcceptor(mSocket, mBalancer);
}

StyxServerManager::~StyxServerManager() {
}

void StyxServerManager::setAddress(const char * hname,
		short port,
		struct sockaddr_in * sap,
		char * protocol) {
	struct hostent *hp;

	memset(sap, 0, sizeof(*sap));
	sap->sin_family = AF_INET;
	if(hname != NULL) {
#ifdef WIN32
			sap->sin_addr.s_addr = inet_addr( hname );
#else
		if(!inet_aton(hname, &sap->sin_addr)) {
			hp = gethostbyname(hname);
			if(hp == NULL)
				throw "unknown host";
			sap->sin_addr = *(struct in_addr *)hp->h_addr;
		}
#endif
	}
	else {
		sap->sin_addr.s_addr = htonl( INADDR_ANY);
	}
	sap->sin_port = htons(port);
}

void StyxServerManager::setNonBlocking(Socket socket) {
#ifdef WIN32
	// If iMode!=0, non-blocking mode is enabled.
	u_long mode=1;
	ioctlsocket(socket, FIONBIO, &mode);
#else
	int opts;
	opts = fcntl(socket,F_GETFL);
	if (opts < 0) {
		throw "Can't create socket fcntl(F_GETFL)";
	}
	opts = (opts | O_NONBLOCK);
	if (fcntl(socket,F_SETFL,opts) < 0) {
		throw "Can't create socket fcntl(F_SETFL)";
	}
#endif
}

void StyxServerManager::start() {
	mAcceptor->start();
}

void StyxServerManager::stop() {
	// TODO implement this
}

// create and bind socket
Socket StyxServerManager::createSocket(string address,
		int port) {
	Socket res = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
#ifdef WIN32
	if ( res == INVALID_SOCKET ) {
#else
	if ( res == -1 ) {
#endif
		throw new StyxLibraryException(__FILE__, "Can't create socket", errno);
	}
	int reuse_addr = 1;
	setsockopt(res, 
		SOL_SOCKET, 
		SO_REUSEADDR, 
		(const char*)&reuse_addr,
		sizeof(reuse_addr));
	// enable non-blocking mode
	this->setNonBlocking(res);
	// resolve address and bind socket
	struct sockaddr_in server_address;
	this->setAddress(address.c_str(), port, &server_address, NULL);
	if (bind(res, 
		(struct sockaddr *) &server_address,
			sizeof(server_address)) < 0 ) {
#ifdef WIN32
		closesocket(res);
#else
		close(res);
#endif
		throw new StyxLibraryException(__FILE__, "Can't bind socket", errno);
		
	}
	return res;
}