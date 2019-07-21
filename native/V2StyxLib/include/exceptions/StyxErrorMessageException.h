/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXERRORMESSAGEEXCEPTION_H_
#define STYXERRORMESSAGEEXCEPTION_H_

#include "messages/StyxRErrorMessage.h"
#include "exceptions/StyxException.h"


class StyxErrorMessageException : public StyxException {
public:
	StyxErrorMessageException(const char *message, ...);
	StyxErrorMessageException(StyxRErrorMessage *message);
	virtual ~StyxErrorMessageException();
	StyxRErrorMessage* constructErrorMessage();
	static void checkException(StyxMessage* answer) throw(StyxException);
};

#endif /* STYXERRORMESSAGEEXCEPTION_H_ */
