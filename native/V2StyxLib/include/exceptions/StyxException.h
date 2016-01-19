/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXEXCEPTION_H_
#define STYXEXCEPTION_H_

class StyxException {
private:
	const char *mMessage;
public:
	StyxException(const char *message);
	virtual ~StyxException();
};

#endif /* STYXEXCEPTION_H_ */
