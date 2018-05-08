/*
 * StyxErrorMessageException.cpp
 *
 *  Created on: Apr 06, 2018
 *      Author: vschryabets@gmail.com
 */

#include "exceptions/StyxException.h"
#include <iostream>

StyxException::StyxException(const char *message, ...) : mInternalCode(NONE){
	va_list arglist;
	va_start( arglist, message );
	setMessage(message, arglist);
	va_end( arglist );
}

StyxException::StyxException(InternalErrors code) : mInternalCode(code) {
	
}

StyxException::~StyxException() {

}

void StyxException::printStackTrace() {
	std::cerr << mMessage << std::endl;
}

StyxString StyxException::getMessage() {
	return mMessage;
}

void StyxException::setMessage(const char *format, va_list arglist) {
	char buf[4096];
	vsnprintf(buf, sizeof(buf), format, arglist);
	this->mMessage = StyxString(buf);
}

InternalErrors StyxException::getInternalCode() {
	return mInternalCode;
}