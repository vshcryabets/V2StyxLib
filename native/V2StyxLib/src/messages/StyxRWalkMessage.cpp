/*
 * StyxRWalkMessage.cpp
 *
 *  Created on: Jun 2, 2012
 *      Author: mrco
 */

#include "StyxRWalkMessage.h"

StyxRWalkMessage::StyxRWalkMessage(StyxTAG tag, std::vector<StyxQID> *QIDList)
	: StyxMessage(Rwalk, tag) {
}

StyxRWalkMessage::~StyxRWalkMessage() {
	// TODO Auto-generated destructor stub
}

