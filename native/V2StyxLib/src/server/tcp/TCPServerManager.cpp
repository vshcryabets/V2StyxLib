/*
 * TCPServerManager.cpp
 *
 *  Created on: Dec 6, 2016
 *      Author: vova
 */

#include "server/tcp/TCPServerManager.h"
#include "server/tcp/TCPServerChannelDriver.h"

TCPServerManager::TCPServerManager(StyxString address, uint16_t port, IVirtualStyxFile* root) :
	StyxServerManager(root, std::vector<IChannelDriver*>()) {
#warning new TCPServerChannelDriver()) in prev line

}

TCPServerManager::~TCPServerManager() {
	// TODO Auto-generated destructor stub
}

