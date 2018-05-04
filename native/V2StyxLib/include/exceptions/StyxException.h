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
	StyxException();
	StyxException(const char *format, ...);
	void setMessage(const char *format, va_list list);
	virtual ~StyxException();
	virtual void printStackTrace();
	StyxString getMessage();
};

#endif /* STYXEXCEPTION_H_ */
