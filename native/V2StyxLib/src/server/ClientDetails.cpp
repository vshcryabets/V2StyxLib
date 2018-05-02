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

ClientDetails::ClientDetails(IChannelDriver* driver, uint32_t id)  {
	mAssignedFiles = new std::map<StyxFID,IVirtualStyxFile*>();
	mPolls = new Polls();
}

ClientDetails::~ClientDetails() {
	delete mPolls;
	delete mAssignedFiles;
}

void ClientDetails::registerOpenedFile(StyxFID fid, IVirtualStyxFile* file) {
	mAssignedFiles->insert(
			std::pair<StyxFID, IVirtualStyxFile*>(fid, file));
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