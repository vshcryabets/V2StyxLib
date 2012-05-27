/*
 * StyxRErrorMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: mrco
 */

#ifndef STYXRERRORMESSAGE_H_
#define STYXRERRORMESSAGE_H_
#include "StyxMessage.h"
#include <string>
#include "StyxRErrorMessage.h"

class StyxRErrorMessage : public StyxMessage {
private:
	std::string *mMessage;
public:
	StyxRErrorMessage(int tag, std::string *message);
	virtual ~StyxRErrorMessage();
};

#endif /* STYXRERRORMESSAGE_H_ */
