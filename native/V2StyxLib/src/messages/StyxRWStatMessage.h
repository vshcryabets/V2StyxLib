/*
 * StyxRWStatMessage.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXRWSTATMESSAGE_H_
#define STYXRWSTATMESSAGE_H_

#include "StyxMessage.h"

class StyxRWStatMessage: public StyxMessage {
public:
	StyxRWStatMessage(StyxTAG tag);
	~StyxRWStatMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
};

#endif /* STYXRWSTATMESSAGE_H_ */
