/*
 * ClientBalancer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "ClientBalancer.h"

ClientBalancer::ClientBalancer(int iounit, IVirtualStyxDirectory *root) {
	mNewConnections = new vector<Socket>();
	mReadable = new vector<Socket>();
	mHandler = new ClientsHandler(iounit, root);
}

ClientBalancer::~ClientBalancer() {
	delete mNewConnections;
	delete mReadable;
}

void ClientBalancer::pushNewConnection(Socket socket) {
	mNewConnections->push_back(socket);
}

void ClientBalancer::pushReadable(Socket socket) {
	mReadable->push_back(socket);
}

void ClientBalancer::process() {
	// new connections
	for (vector<Socket>::iterator socketIterator = mNewConnections->begin();
			socketIterator < mNewConnections->end(); socketIterator++ ) {
		mHandler->addClient(*socketIterator);
	}
	mNewConnections->clear();
	// new readables
	for (vector<Socket>::iterator socketIterator = mReadable->begin();
			socketIterator < mReadable->end(); socketIterator++ ) {
//		boolean closed = mHandler.readClient(channel);
	}
	mReadable->clear();
}
