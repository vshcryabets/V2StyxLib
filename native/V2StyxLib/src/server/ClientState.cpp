/*
 * ClientState.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "ClientState.h"
#include "stdio.h"
#include "../messages/StyxMessage.h"
#include "../messages/StyxRVersionMessage.h"
#include "../messages/StyxTAttachMessage.h"
#include "../StyxErrorMessageException.h"

ClientState::ClientState(size_t iounit,
		Socket channel,
		IVirtualStyxDirectory *root,
		std::string *protocol) : mIOUnit(iounit),
		mChannel(channel), mServerRoot(root),
		mProtocol(protocol) {
	mBuffer = new StyxByteBufferReadable(mIOUnit*2);
	mAssignedFiles = new std::map<unsigned int32_t,IVirtualStyxFile*>();
	mOutputBuffer = new StyxByteBufferWritable(iounit);
}

ClientState::~ClientState() {
	delete mOutputBuffer;
	delete mBuffer;
	delete mAssignedFiles;
}

/**
 *
 * @return true if message was processed
 */
bool ClientState::process() {
	ssize_t inBuffer = mBuffer->remainsToRead();
	if ( inBuffer > 4 ) {
		ssize_t packetSize = mBuffer->getUInt32();
		if ( inBuffer >= packetSize ) {
			StyxMessage *message = StyxMessage::factory(mBuffer, mIOUnit);
			processMessage(message);
			return true;
		}
	}
	return false;
}

/**
 * Processing incoming messages
 * @param msg incomming message
 */
void ClientState::processMessage(StyxMessage *msg) {
	//        System.out.print("Got message "+msg.toString());
	StyxMessage *answer = NULL;
	long fid;
	try {
		switch (msg->getType()) {
		case Tversion:
			answer = new StyxRVersionMessage(mIOUnit, mProtocol);
			break;
		case Tattach:
			answer = processAttach((StyxTAttachMessage*)msg);
			break;
		/*case Tauth:
			answer = processAuth((StyxTAuthMessage)msg);
			break;
		case Tstat:
			fid = ((StyxTStatMessage)msg).getFID();
			file = mAssignedFiles.get(fid);
			if ( file != null ) {
				answer = new StyxRStatMessage(msg.getTag(), file.getStat());
			} else {
				answer = getNoFIDError(msg, fid);
			}
			break;
		case Tclunk:
			fid = ((StyxTClunkMessage)msg).getFID();
			file = mAssignedFiles.remove(fid);
			if ( file == null ) {
				answer = getNoFIDError(msg, fid);
			} else {
				file.close(this);
				answer = new StyxRClunkMessage(msg.getTag());
			}
			break;
		case Tflush:
			// TODO do something there
			answer = new StyxRFlushMessage(msg.getTag());
			break;
		case Twalk:
			answer = processWalk((StyxTWalkMessage) msg);
			break;
		case Topen:
			answer = processTopen((StyxTOpenMessage)msg);
			break;
		case Tread:
			answer = processTread((StyxTReadMessage)msg);
			break;
		case Twrite:
			answer = processWrite((StyxTWriteMessage)msg);
			break;
		case Twstat:
			answer = processWStat((StyxTWStatMessage)msg);*/
		default:
			printf("Got unknown message:");
			//			System.out.println(msg.toString());
			break;
		}
	} catch (StyxErrorMessageException *e) {
		answer = e->getErrorMessage();
		answer->setTag(msg->getTag());
	}
	if ( answer != NULL ) {
		sendMessage(answer);
	}
}

void ClientState::sendMessage(StyxMessage *answer) {
	int writed = answer->writeToBuffer(mOutputBuffer);
	::write(mChannel, mOutputBuffer->getBuffer(), writed);
}

bool ClientState::readSocket() {
	int readCount = mBuffer->readFromFD(mChannel);
	//	int readCount = read(mChannel, mBuffer+mBufferPosition, mIOUnit);
	//	mBufferPosition+=readCount;
	if ( readCount < -1 ) {
		return true;
	} else {
		while ( process() );
	}
	return false;
}

