/*
 * FIDPoll.h
 *
 */

#ifndef INCLUDE_UTILS_FIDPOLL_H_
#define INCLUDE_UTILS_FIDPOLL_H_

#include "utils/AbstractPoll.h"

class FIDPoll : public AbstractPoll<StyxFID> {
public:
	FIDPoll();
	virtual ~FIDPoll();
};

#endif /* INCLUDE_UTILS_FIDPOLL_H_ */
