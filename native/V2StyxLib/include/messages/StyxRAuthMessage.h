/*
 * StyxRAuthMessage.h
 *
 */

#ifndef STYXRAUTHMESSAGE_H_
#define STYXRAUTHMESSAGE_H_
#include "messages/base/structs/StyxQID.h"
#include "./messages/base/StyxRSingleQIDMessage.h"

class StyxRAuthMessage : public StyxRSingleQIDMessage {
public:
	StyxRAuthMessage(StyxTAG tag, const StyxQID *qid);
	virtual ~StyxRAuthMessage();
};

#endif /* STYXRAUTHMESSAGE_H_ */
