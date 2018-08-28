/*
 * TCPServerManager.cpp
 *
 *  Created on: Dec 6, 2016
 *      Author: vova
 */

#include "server/tcp/TCPServerManager.h"
#include "server/tcp/TCPServerChannelDriver.h"

TCPServerManager::TCPServerManager(StyxString address, uint16_t port, IVirtualStyxFile* root) :
	StyxServerManager(root), mDriver(new TCPServerChannelDriver(address, port)) {
	addDriver(mDriver);
}

TCPServerManager::~TCPServerManager() {
	delete mDriver;
}

