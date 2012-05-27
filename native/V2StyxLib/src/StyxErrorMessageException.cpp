/*
 * StyxErrorMessageException.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxErrorMessageException.h"

StyxErrorMessageException::StyxErrorMessageException() {
	// TODO Auto-generated constructor stub

}

StyxErrorMessageException::~StyxErrorMessageException() {
	// TODO Auto-generated destructor stub
}

StyxRErrorMessage* StyxErrorMessageException::getErrorMessage() {
	return mMessage;
}
