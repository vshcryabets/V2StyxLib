/*
 * ClientState.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "ClientState.h"
#include "stdio.h"

ClientState::ClientState(int iounit,
		Socket channel,
		IVirtualStyxDirectory *root) : mIOUnit(iounit), mChannel(channel), mServerRoot(root) {
	mBuffer = new DualStateBuffer(mIOUnit*2);
	mAssignedFiles = new std::map<unsigned int32_t,IVirtualStyxFile*>();
}

ClientState::~ClientState() {
	delete [] mBuffer;
	delete mAssignedFiles;
}

bool ClientState::process() {
	ssize_t inBuffer = mBuffer->remainsToRead();
    if ( inBuffer > 4 ) {
        ssize_t packetSize = mBuffer->getUInt32();
//        if ( inBuffer >= packetSize ) {
//            final StyxMessage message = StyxMessage.factory(mBuffer, mIOUnit);
//            processMessage(message);
//            return true;
//        }
    }
	printf((const char*)mBuffer);
    return false;
}

bool ClientState::readSocket() {
	size_t readCount = mBuffer->readFromFD(mChannel);
//	int readCount = read(mChannel, mBuffer+mBufferPosition, mIOUnit);
//	mBufferPosition+=readCount;
    if ( readCount < -1 ) {
        return true;
    } else {
        while ( process() );
    }
    return false;
}

