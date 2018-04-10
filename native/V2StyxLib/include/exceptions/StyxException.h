/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXEXCEPTION_H_
#define STYXEXCEPTION_H_

#include "types.h"

class StyxException {
private:
	StyxString mMessage;
public:
	StyxException(const char *format, ...);
	StyxException(StyxString message, ...);
	virtual ~StyxException();
};

#endif /* STYXEXCEPTION_H_ */
