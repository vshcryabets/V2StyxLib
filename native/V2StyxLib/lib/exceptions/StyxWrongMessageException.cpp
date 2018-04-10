/*
 * StyxWrongMessageException.cpp
 *
 *  Created on: Apr 08, 2018
 *      Author: vschryabets@gmail.com
 */

#include "exceptions/StyxWrongMessageException.h"

StyxWrongMessageException::StyxWrongMessageException(StyxMessage* received, MessageType needed) :
	StyxException("Recived message of type %d when needed %d.", received->getType(), needed ) {
}

StyxWrongMessageException::~StyxWrongMessageException() {

}
