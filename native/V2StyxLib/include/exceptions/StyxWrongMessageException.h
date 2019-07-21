/*
 * StyxWrongMessageException.h
 *
 *  Created on: Apr 06, 2018
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXWRONGMESSAGEEXCEPTION_H_
#define STYXWRONGMESSAGEEXCEPTION_H_

#include "exceptions/StyxException.h"
#include "messages/base/StyxMessage.h"

class StyxWrongMessageException : public StyxException {
public:
	StyxWrongMessageException(StyxMessage* received, MessageType needed);
	virtual ~StyxWrongMessageException();
};

#endif /* STYXWRONGMESSAGEEXCEPTION_H_ */
