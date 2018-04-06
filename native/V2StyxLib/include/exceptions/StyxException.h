/*
 * StyxErrorMessageException.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXEXCEPTION_H_
#define STYXEXCEPTION_H_

#include <string>

class StyxException {
private:
	std::string mMessage;
public:
	StyxException(const char *message);
	StyxException(std::string message);
	virtual ~StyxException();
};

#endif /* STYXEXCEPTION_H_ */
