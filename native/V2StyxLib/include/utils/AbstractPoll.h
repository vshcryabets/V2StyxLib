/*
 * AbstractPoll.h
 *
 */

#ifndef INCLUDE_UTILS_ABSTRACTPOLL_H_
#define INCLUDE_UTILS_ABSTRACTPOLL_H_

template <class T>
class AbstractPoll {
public:
	AbstractPoll();
	virtual ~AbstractPoll();

	virtual T getFreeItem();
	virtual bool release(T id);
	virtual void clean();
	virtual T getNext() = 0;
};

#endif /* INCLUDE_UTILS_ABSTRACTPOLL_H_ */
