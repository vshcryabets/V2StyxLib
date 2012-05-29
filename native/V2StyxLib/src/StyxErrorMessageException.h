/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXERRORMESSAGEEXCEPTION_H_
#define STYXERRORMESSAGEEXCEPTION_H_
#include "messages/StyxRErrorMessage.h"

class StyxErrorMessageException {
private:
	StyxRErrorMessage *mMessage;
public:
	StyxErrorMessageException();
	virtual ~StyxErrorMessageException();
	StyxRErrorMessage* getErrorMessage();
};

#endif /* STYXERRORMESSAGEEXCEPTION_H_ */
