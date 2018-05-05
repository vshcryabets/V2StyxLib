/*
 * Polls.h
 *
 *  Created on: Dec 3, 2016
 *      Author: mrco
 */

#ifndef INCLUDE_UTILS_POLLS_H_
#define INCLUDE_UTILS_POLLS_H_

#include "utils/FIDPoll.h"
#include <map>
#include "messages/base/StyxTMessage.h"
#include "messages/base/StyxTMessageFID.h"

class Polls {
protected:
	std::map<StyxTAG, StyxTMessage*> mMessagesMap;
    MessageTagPoll *mTags;
    FIDPoll *mFids;
public:
	Polls();
	virtual ~Polls();

    FIDPoll* getFIDPoll();
    MessageTagPoll* getTagPoll();
    void putTMessage(StyxTAG tag, StyxTMessage* message);
    StyxTMessage* getTMessage(StyxTAG tag); // TODO probably we can use one call instead getTMessage && releaseTag
    void releaseTag(StyxTAG tag);
    void releaseFID(StyxTMessageFID* message);
};

#endif /* INCLUDE_UTILS_POLLS_H_ */
