/*
 * FIDPoll.h
 *
 */

#ifndef INCLUDE_UTILS_FIDPOLL_H_
#define INCLUDE_UTILS_FIDPOLL_H_

#include "utils/AbstractPoll.h"
#include "types.h"

class FIDPoll : public AbstractPoll<StyxFID> {
protected:
	static const uint32_t MAXUNINT = 0xFFFFFFFF;
public:
	FIDPoll();
	virtual ~FIDPoll();

	virtual bool release(StyxFID id);
	virtual StyxFID getNext();
	virtual void clean();
};

#endif /* INCLUDE_UTILS_FIDPOLL_H_ */
