/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXEXCEPTION_H_
#define STYXEXCEPTION_H_

#include "types.h"

enum InternalErrors {
	NONE = 0,
	// Channel driver
	DRIVER_CREATE_ERROR = 0x1000,
	DRIVER_BIND_ERROR,
	DRIVER_CONFIGURE_ERROR,
	DRIVER_CANT_RESOLVE_NAME
};

class StyxException {
public:
	StyxException(InternalErrors code);
	StyxException(const char *format, ...);
	void setMessage(const char *format, va_list list);
	virtual ~StyxException();
	virtual void printStackTrace();
	StyxString getMessage();
	InternalErrors getInternalCode();
private:
	StyxString mMessage;
	InternalErrors mInternalCode;
};

#endif /* STYXEXCEPTION_H_ */
