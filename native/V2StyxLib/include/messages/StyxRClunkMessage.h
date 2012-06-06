/*
 * StyxRClunkMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXRCLUNKMESSAGE_H_
#define STYXRCLUNKMESSAGE_H_

#include "StyxMessage.h"

class StyxRClunkMessage: public StyxMessage {
public:
	StyxRClunkMessage(StyxTAG tag);
	virtual ~StyxRClunkMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
};

#endif /* STYXRCLUNKMESSAGE_H_ */
