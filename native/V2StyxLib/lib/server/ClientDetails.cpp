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

ClientDetails::ClientDetails(IChannelDriver* driver, uint32_t id)  {
	mAssignedFiles = new std::map<StyxFID,IVirtualStyxFile*>();
}

ClientDetails::~ClientDetails() {
	delete mAssignedFiles;
}

void ClientDetails::registerOpenedFile(StyxFID fid, IVirtualStyxFile* file) {
	mAssignedFiles->insert(
			std::pair<StyxFID, IVirtualStyxFile*>(fid, file));
}
