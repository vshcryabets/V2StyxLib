/*
 * MessageTagPoll.h
 *
 */

#ifndef INCLUDE_UTILS_MESSAGETAGPOLL_H_
#define INCLUDE_UTILS_MESSAGETAGPOLL_H_

#include "utils/AbstractPoll.h"

class MessageTagPoll : public AbstractPoll<StyxTAG> {
public:
	MessageTagPoll();
	virtual ~MessageTagPoll();
};

#endif /* INCLUDE_UTILS_MESSAGETAGPOLL_H_ */
