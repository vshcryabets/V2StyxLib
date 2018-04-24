/*
 * StyxErrorMessageException.cpp
 *
 *  Created on: Apr 06, 2018
 *      Author: vschryabets@gmail.com
 */

#include "exceptions/StyxException.h"
#include <iostream>

StyxException::StyxException(const char *message, ...) {
	char buf[4096];
	va_list arglist;
	va_start( arglist, message );
	vsnprintf(buf, sizeof(buf), message, arglist);
	va_end( arglist );
	this->mMessage = StyxString(buf);
}

StyxException::StyxException(StyxString message, ...) {
	char buf[4096];
	va_list arglist;
	va_start( arglist, message.c_str() );
	vsnprintf(buf, sizeof(buf), message.c_str(), arglist);
	va_end( arglist );
	this->mMessage = StyxString(buf);
}

StyxException::~StyxException() {

}

void StyxException::printStackTrace() {
	std::cerr << mMessage << std::endl;
}
