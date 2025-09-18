/*
 * StyxRClunkMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "messages/StyxRClunkMessage.h"

StyxRClunkMessage::StyxRClunkMessage(StyxTAG tag) :
	StyxMessage(Rclunk, tag){
}

StyxRClunkMessage::~StyxRClunkMessage() {
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRClunkMessage::load(IStyxDataReader *input) {
	return;
}


