/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXERRORMESSAGEEXCEPTION_H_
#define STYXERRORMESSAGEEXCEPTION_H_
#include "./messages/StyxRErrorMessage.h"

class StyxErrorMessageException {
private:
	StyxRErrorMessage *mMessage;
public:
	StyxErrorMessageException(const char *message);
	StyxErrorMessageException(StyxRErrorMessage *message);
	virtual ~StyxErrorMessageException();
	StyxRErrorMessage* getErrorMessage();
};

#endif /* STYXERRORMESSAGEEXCEPTION_H_ */
