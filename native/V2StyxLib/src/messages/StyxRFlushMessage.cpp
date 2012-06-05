/*
 * StyxRFlushMessage.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "StyxRFlushMessage.h"

StyxRFlushMessage::StyxRFlushMessage(StyxTAG tag) :
	StyxMessage(Rflush, tag){
	// TODO Auto-generated constructor stub

}

StyxRFlushMessage::~StyxRFlushMessage() {
	// TODO Auto-generated destructor stub
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRFlushMessage::load(IStyxDataReader *input) {

}
