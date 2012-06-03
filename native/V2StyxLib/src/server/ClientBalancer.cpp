/*
 * ClientBalancer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "ClientBalancer.h"
#include "stdio.h"

ClientBalancer::ClientBalancer(int iounit, IVirtualStyxDirectory *root, std::string *protocol) {
	mNewConnections = new vector<Socket>();
	mReadable = new vector<Socket>();
	mAllConnections = new vector<Socket>();
	mHandler = new ClientsHandler(iounit, root, protocol);
}

ClientBalancer::~ClientBalancer() {
	delete mNewConnections;
	delete mReadable;
	delete mAllConnections;
}

void ClientBalancer::pushNewConnection(Socket socket) {
	printf("New conection\n");
	mNewConnections->push_back(socket);
}

void ClientBalancer::pushReadable(Socket socket) {
	printf("New data\n");
	mReadable->push_back(socket);
}

void ClientBalancer::process() {
	// new connections
	for (vector<Socket>::iterator socketIterator = mNewConnections->begin();
			socketIterator < mNewConnections->end(); socketIterator++ ) {
		printf("FD=%d\n",*socketIterator);
		mHandler->addClient(*socketIterator);
		mAllConnections->push_back(*socketIterator);
	}
	mNewConnections->clear();

	// new readables
	for (vector<Socket>::iterator socketIterator = mReadable->begin();
			socketIterator != mReadable->end(); ) {
		bool closed = mHandler->readClient(*socketIterator);
		if ( closed ) {
			::close(*socketIterator);
			socketIterator = mAllConnections->erase(socketIterator);
		} else {
			socketIterator++;
		}
	}
	mReadable->clear();
}
vector<Socket> *ClientBalancer::getAllConnections() {
	return mAllConnections;
}
