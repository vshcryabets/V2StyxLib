/*
 * StyxServerManager.cpp
 *
 *  Created on: May 20, 2012
 *      Author: mrco
 */

#include "StyxServerManager.h"

StyxServerManager::StyxServerManager(string address,
		int port,
		IVirtualStyxDirectory *root):mPort(port), mRoot(root) {
	// resolve address
	// bind socket
}

StyxServerManager::~StyxServerManager() {
	// TODO Auto-generated destructor stub
}

void StyxServerManager::start() {

}

void StyxServerManager::close() {

}

