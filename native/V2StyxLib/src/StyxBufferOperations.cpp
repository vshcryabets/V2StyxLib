/*
 * StyxBufferOperations.cpp
 *
 *  Created on: May 22, 2012
 *      Author: mrco
 */

#include "StyxBufferOperations.h"

StyxBufferOperations::StyxBufferOperations() {
	mDataBuffer = new int8_t[sDataBufferSize];
}

StyxBufferOperations::~StyxBufferOperations() {
	delete [] mDataBuffer;
}

uint32_t StyxBufferOperations::getUInt32() {return getInteger(4);}
