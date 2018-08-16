/*
 * ClientState.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "server/ClientDetails.h"
#include "stdio.h"
#include "messages/base/StyxMessage.h"
#include "exceptions/StyxErrorMessageException.h"
#include <vector>
#include <unistd.h>
#include <sstream>

ClientDetails::ClientDetails(IChannelDriver* driver, uint32_t id) 
	: mDriver(driver), mClientId(id), mCredentials("", "") {
	if (mDriver == NULL) {
		throw StyxException("Driver is null");
	}
	mPolls = new Polls();
}

ClientDetails::~ClientDetails() {
	delete mPolls;
}

void ClientDetails::registerOpenedFile(StyxFID fid, IVirtualStyxFile* file) {
	mAssignedFiles.insert(std::pair<StyxFID, IVirtualStyxFile*>(fid, file));
}

Polls *ClientDetails::getPolls() {
	return mPolls;
}

StyxString ClientDetails::toString() {
	std::stringstream stream;
	stream << "id " << mClientId;
	if ( mDriver != NULL ) {
		stream << " driver " << mDriver->toString();
	}
	return stream.str();
}

IVirtualStyxFile* ClientDetails::getAssignedFile(StyxFID fid) throw(StyxErrorMessageException) {
	std::map<StyxFID, IVirtualStyxFile*>::iterator it = mAssignedFiles.find(fid);
	if (it == mAssignedFiles.end() ) {
		throw StyxErrorMessageException("Unknown FID (%d)", fid);
	}
	return it->second;
}

IChannelDriver* ClientDetails::getDriver() {
	return mDriver;
}

void ClientDetails::setCredentials(Credentials credentials) {
	mCredentials = credentials;
}

void ClientDetails::unregisterClosedFile(StyxFID fid) {
	std::map<StyxFID, IVirtualStyxFile*>::iterator it = mAssignedFiles.find(fid);
	if (it == mAssignedFiles.end()) {
		throw StyxErrorMessageException("Unknown FID (%d)", fid);
	}
	mAssignedFiles.erase(it);
	}