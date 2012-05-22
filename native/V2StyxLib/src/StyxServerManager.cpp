/*
 * StyxServerManager.cpp
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "StyxServerManager.h"
#include "sys/socket.h"
#include "fcntl.h"
#include "string.h"
#include "netinet/in.h"
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include "ClientBalancer.h"
#include "ConnectionAcceptor.h"


StyxServerManager::StyxServerManager(string address,
		int port,
		IVirtualStyxDirectory *root):mPort(port), mRoot(root), mIOBufSize(8192) {
	// create socket
	mSocket = socket(AF_INET, SOCK_STREAM, 0);
	if ( mSocket == -1 ) {
		throw "Can't create socket socket()";
	}
	int reuse_addr = 1;
	setsockopt(mSocket, SOL_SOCKET, SO_REUSEADDR, &reuse_addr,
			sizeof(reuse_addr));
	// enable non-blocking mode
	this->setNonBlocking(mSocket);
	// resolve address and bind socket
	struct sockaddr_in server_address;
	this->setAddress(address.c_str(), port, &server_address, NULL);
	if (bind(mSocket, (struct sockaddr *) &server_address,
			sizeof(server_address)) < 0 ) {
		close(mSocket);
		throw "Can't create socket bind()";
	}

    mBalancer = new ClientBalancer(mIOBufSize, root);
    mAcceptor = new ConnectionAcceptor(mSocket, mBalancer);
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

void StyxServerManager::deaWithData(int* list, int idx) {
	char buffer[80];     /* Buffer for socket reads */
	char *cur_char;      /* Used in processing buffer */

	ssize_t readed = read(list[idx], buffer, 80);
	if (readed < 0) {
		/* Connection closed, close this end
		   and free up entry in connectlist */
		close(list[idx]);
		list[idx] = 0;
	} else {
		/* We got some data, so upper case it
		   and send it back. */
		cur_char = buffer;
		while (cur_char[0] != 0) {
			cur_char[0] = toupper(cur_char[0]);
			cur_char++;
		}
		write(list[idx],(const void*)buffer,readed);
	}
}

void StyxServerManager::setNonBlocking(Socket socket) {
	int opts;
	opts = fcntl(socket,F_GETFL);
	if (opts < 0) {
		throw "Can't create socket fcntl(F_GETFL)";
	}
	opts = (opts | O_NONBLOCK);
	if (fcntl(socket,F_SETFL,opts) < 0) {
		throw "Can't create socket fcntl(F_SETFL)";
	}
}

void StyxServerManager::start() {
	mAcceptor->start();
}

void StyxServerManager::stop() {

}

