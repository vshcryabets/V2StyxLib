/*
 * StyxRVersionMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxRAttachMessage.h"

StyxRAttachMessage::StyxRAttachMessage(StyxTAG tag, const StyxQID qid)
	: StyxRSingleQIDMessage( Rattach, tag, qid ) {
}

StyxRAttachMessage::~StyxRAttachMessage() {
	// TODO Auto-generated destructor stub
}
