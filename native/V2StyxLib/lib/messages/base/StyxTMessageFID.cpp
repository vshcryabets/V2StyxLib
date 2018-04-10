/*
 * StyxTMessageFID.cpp
 *
 *  Created on: Apr 07, 2018
 *      Author: vschryabets@gmail.com
 */

#include "messages/base/StyxTMessageFID.h"

StyxTMessageFID::StyxTMessageFID(MessageTypeEnum type, MessageTypeEnum answer, StyxFID fid)
	: StyxTMessage(type, answer),
	  mFID(fid) {
}

StyxTMessageFID::~StyxTMessageFID() {
}

StyxFID StyxTMessageFID::getFID() {
	return mFID;
}

void StyxTMessageFID::setFID(StyxFID fid) {
	mFID = fid;
}

