/*
 * StyxRFlushMessage.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXRFLUSHMESSAGE_H_
#define STYXRFLUSHMESSAGE_H_

#include "StyxMessage.h"

class StyxRFlushMessage: public StyxMessage {
public:
	StyxRFlushMessage(StyxTAG tag);
	~StyxRFlushMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
};

#endif /* STYXRFLUSHMESSAGE_H_ */
