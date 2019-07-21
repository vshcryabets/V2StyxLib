/*
 * StyxRAuthMessage.cpp
 *
 *  Created on: May 4, 2018
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxRAuthMessage.h"

StyxRAuthMessage::StyxRAuthMessage(StyxTAG tag, StyxQID qid)
	: StyxRSingleQIDMessage( Rauth, tag, qid ) {
}

StyxRAuthMessage::~StyxRAuthMessage() {
	// TODO Auto-generated destructor stub
}
