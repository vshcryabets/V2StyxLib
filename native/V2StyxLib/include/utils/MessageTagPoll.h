/*
 * MessageTagPoll.h
 *
 */

#ifndef INCLUDE_UTILS_MESSAGETAGPOLL_H_
#define INCLUDE_UTILS_MESSAGETAGPOLL_H_

#include "utils/FIDPoll.h"

class MessageTagPoll : public AbstractPoll<StyxTAG> {
public:
	MessageTagPoll();
	virtual ~MessageTagPoll();
	virtual StyxTAG getNext();
	virtual bool release(StyxTAG id);
};

#endif /* INCLUDE_UTILS_MESSAGETAGPOLL_H_ */
