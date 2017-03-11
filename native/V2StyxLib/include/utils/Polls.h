/*
 * Polls.h
 *
 *  Created on: Dec 3, 2016
 *      Author: mrco
 */

#ifndef INCLUDE_UTILS_POLLS_H_
#define INCLUDE_UTILS_POLLS_H_

#include "utils/FIDPoll.h"
#include "utils/MessageTagPoll.h"
#include <map>

class Polls {
public:
	Polls();
	virtual ~Polls();

    FIDPoll* getFIDPoll();
    MessageTagPoll* getTagPoll();
    std::map<StyxTAG, StyxTMessage>* getMessagesMap();
    void releaseTag(int tag);
    void releaseFID(StyxTMessageFID message);
};

#endif /* INCLUDE_UTILS_POLLS_H_ */
