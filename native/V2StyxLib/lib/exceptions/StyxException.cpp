/*
 * StyxErrorMessageException.cpp
 *
 *  Created on: Apr 06, 2018
 *      Author: vschryabets@gmail.com
 */

#include "exceptions/StyxException.h"

StyxException::StyxException(const char *message) {
	this->mMessage = std::string(message);
}

StyxException::StyxException(std::string message) {
	this->mMessage = message;
}

StyxException::~StyxException() {

}

