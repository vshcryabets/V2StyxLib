/*
 * ConnectionAcceptor.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "ConnectionAcceptor.h"
#include "sys/socket.h"
#include "fcntl.h"
#include "string.h"
#include "netinet/in.h"
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include "ClientBalancer.h"
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
		size_t readsocks = select(highsock+1,
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
					this->setNonBlocking(inSocket);
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

void ConnectionAcceptor::setNonBlocking(Socket socket) {
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

void ConnectionAcceptor::stop() {

}

