/*
 * StyxServerManager.cpp
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "library/StyxServerManager.h"

const StyxString StyxServerManager::PROTOCOL = "9P2000";
const size_t StyxServerManager::DEFAULT_IOUNIT = 8192;
const size_t StyxServerManager::DEFAULT_TIMEOUT = 5000;

StyxServerManager::StyxServerManager(IVirtualStyxFile *root, std::vector<IChannelDriver*> drivers)
	: mDrivers(drivers), mRoot(root) {
	ConnectionDetails details(getProtocol(), getIOUnit());
    mBalancer = new TMessagesProcessor(details, root);
}
