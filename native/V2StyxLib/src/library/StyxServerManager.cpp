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
	: mRoot(root) {
	// TODO inheritance not works in constructor
	ConnectionDetails details(getProtocol(), getIOUnit());
    mBalancer = new TMessagesProcessor(details, root);
	for ( std::vector<IChannelDriver*>::iterator it = drivers.begin(); 
		it != drivers.end(); ++it ) {
		addDriver(*it);
	}
}

StyxServerManager::~StyxServerManager() {
	delete mBalancer;
}

std::vector<IChannelDriver*> StyxServerManager::getDrivers() {
	return mDrivers;
}

StyxString StyxServerManager::getProtocol() {
	return PROTOCOL;
}

void StyxServerManager::closeAndWait() {
	close();
	for ( std::vector<StyxThread*>::iterator it = mDriverThreads.begin(); 
		it != mDriverThreads.end(); ++it ) {
		(*it)->join();
	}
}

void StyxServerManager::close() {
	mBalancer->close();
	for ( std::vector<IChannelDriver*>::iterator it = mDrivers.begin(); 
		it != mDrivers.end(); ++it ) {
		(*it)->close();
	}
}

std::vector<StyxThread*> StyxServerManager::start() {
	size_t count = mDrivers.size();
	size_t ioUnit = getIOUnit();
	mDriverThreads.clear();
	for ( std::vector<IChannelDriver*>::iterator it = mDrivers.begin(); 
		it != mDrivers.end(); ++it ) {
		mDriverThreads.push_back( (*it)->start(ioUnit));
	}
	return mDriverThreads;
}

IVirtualStyxFile* StyxServerManager::getRoot() {
	return mRoot;
}

StyxServerManager* StyxServerManager::addDriver(IChannelDriver* driver) throw(StyxException) {
	if (!mDriverThreads.empty()) {
		// we already called start
		throw StyxException("Start() already called");
	}
	if (driver == NULL) {
		throw StyxException("Driver is null");
	}
	mDrivers.push_back(driver);
	driver->setTMessageHandler(mBalancer);
	driver->setRMessageHandler(mBalancer);
	return this;
}

size_t StyxServerManager::getIOUnit() {
	return DEFAULT_IOUNIT;
}