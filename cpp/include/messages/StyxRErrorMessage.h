/*
 * StyxRErrorMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRERRORMESSAGE_H_
#define STYXRERRORMESSAGE_H_
#include "StyxMessage.h"
#include <string>
#include "StyxRErrorMessage.h"
#include "../types.h"

class StyxRErrorMessage : public StyxMessage {
private:
	StyxString *mMessage;
public:
	StyxRErrorMessage(StyxTAG tag, std::string message);
	StyxRErrorMessage(StyxTAG tag, const char *message);
//	StyxRErrorMessage(StyxTAG tag, const char *fmt, ...);
	virtual ~StyxRErrorMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRERRORMESSAGE_H_ */
