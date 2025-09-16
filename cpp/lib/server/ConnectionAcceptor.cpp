/*
 * ConnectionAcceptor.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "server/ConnectionAcceptor.h"
#include "StyxServerManager.h"

#ifdef WIN32
#include <WinSock2.h>
#else
#include "sys/socket.h"
#include "fcntl.h"
#include "netinet/in.h"
#include <arpa/inet.h>
#include <netdb.h>
#endif
#include "string.h"
#include <stdio.h>
#include "server/ClientBalancer.h"
#include <vector>
using namespace std;

ConnectionAcceptor::ConnectionAcceptor(Socket socket, ClientBalancer *balancer) :
		mSocket(socket), mBalancer(balancer) {
}

ConnectionAcceptor::~ConnectionAcceptor() {
}

void ConnectionAcceptor::start() {
	/* Set up queue for incoming connections. */
	listen(mSocket,5);

	/* Since we start with only one socket, the listening socket,
			   it is the highest socket so far. */
	int highsock = mSocket;
	vector<Socket> *connectionList = mBalancer->getAllConnections();
	fd_set socks;
	struct timeval timeout;

	while (true) { /* Main server loop - forever */
		FD_ZERO(&socks);
		FD_SET(mSocket,&socks);

		for ( vector<Socket>::iterator it = connectionList->begin();
				it < connectionList->end();
				it++ ) {
			if ( *it > highsock ) {
				highsock = *it;
			}
			FD_SET(*it, &socks);
		}

		timeout.tv_sec = 1;
		timeout.tv_usec = 0;
		int readsocks = select(highsock+1,
				&socks,
				(fd_set*) 0,
				(fd_set*) 0,
				&timeout);
		if (readsocks < 0) {
			// something wrong with our socket
			return;
		} else if (readsocks > 0) {
			if (FD_ISSET( mSocket, &socks )) {
				Socket inSocket = accept(mSocket, NULL, NULL);
				if ( inSocket > 0 ) {
					StyxServerManager::setNonBlocking(inSocket);
					mBalancer->pushNewConnection(inSocket);
				}
			}
			for ( vector<Socket>::iterator it = connectionList->begin();
					it < connectionList->end();
					it++ ) {
				if (FD_ISSET(*it, &socks)) {
					mBalancer->pushReadable(*it);
				}
			}
			mBalancer->process();
		}
	}
}

void ConnectionAcceptor::stop() {

}

