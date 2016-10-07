/*
 * ClientState.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "server/ClientState.h"
#include "stdio.h"
#include "messages/StyxMessage.h"
#include "messages/StyxRVersionMessage.h"
#include "messages/StyxRAttachMessage.h"
#include "messages/StyxTAttachMessage.h"
#include "messages/StyxRStatMessage.h"
#include "messages/StyxTOpenMessage.h"
#include "messages/StyxROpenMessage.h"
#include "messages/StyxRReadMessage.h"
#include "messages/StyxRClunkMessage.h"
#include "messages/StyxRFlushMessage.h"
#include "messages/StyxRWriteMessage.h"
#include "messages/StyxRWStatMessage.h"
#include "StyxErrorMessageException.h"
#include <vector>
#include <unistd.h>

ClientDetails::ClientDetails(size_t iounit,
		Socket channel,
		IVirtualStyxFile *root,
		std::string protocol)  {
	mIOUnit = iounit;
	mChannel = channel;
	mProtocol = protocol;
	mServerRoot = root;
	mBuffer = new StyxByteBufferReadable(mIOUnit*2);
	mAssignedFiles = new std::map<StyxFID,IVirtualStyxFile*>();
	mOutputBuffer = new StyxByteBufferWritable(iounit);
}

ClientDetails::~ClientDetails() {
	delete mOutputBuffer;
	delete mBuffer;
	delete mAssignedFiles;
}

/**
 *
 * @return true if message was processed
 */
bool ClientDetails::process() {
	size_t inBuffer = mBuffer->remainsToRead();
	//	::printf("inBuffer=%p\n", inBuffer);
	if ( inBuffer > 4 ) {
		size_t packetSize = mBuffer->getUInt32();
		//		::printf("PacketSize=%d\n", packetSize);
		if ( inBuffer >= packetSize ) {
//			::printf("We have new message!\n");
			StyxMessage *message = StyxMessage::factory(mBuffer, mIOUnit);
//			::printf("We have new message type=%d\n", message->getType());
			processMessage(message);
			delete message;
			return true;
		}
	}
	return false;
}

/**
 * Processing incoming messages
 * @param msg incomming message
 */
void ClientDetails::processMessage(StyxMessage *msg) {
	if ( msg == NULL ) {
		printf("Got unknown message:\n");
		return;
	}
	//        System.out.print("Got message "+msg.toString());
	StyxMessage *answer = NULL;
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
			break;*/
		case Tstat:
			answer = processStat((StyxTStatMessage*)msg);
			break;
		case Tclunk:
			answer = processClunk((StyxTCreateMessage*) msg);
			break;
		case Tflush:
			// TODO do something there
			answer = new StyxRFlushMessage(msg->getTag());
			break;
		case Twalk:
			answer = processWalk((StyxTWalkMessage*) msg);
			break;
		case Topen:
			answer = processTopen((StyxTOpenMessage*)msg);
			break;
		case Tread:
			answer = processRead((StyxTReadMessage*)msg);
			break;
		case Twrite:
			answer = processWrite((StyxTWriteMessage*)msg);
			break;
		case Twstat:
			answer = processWStat((StyxTWStatMessage*)msg);
			break;
		default:
			printf("Got unknown message:\n");
			//			System.out.println(msg.toString());
			break;
		}
	} catch (StyxErrorMessageException *e) {
		answer = e->getErrorMessage();
		answer->setTag(msg->getTag());
	}
	if ( answer != NULL ) {
		sendMessage(answer);
		delete answer;
	}
}

void ClientDetails::sendMessage(StyxMessage *answer) {
	size_t inbuffer = answer->writeToBuffer(mOutputBuffer);
#ifdef WIN32
	size_t writed = ::send(mChannel, (const char*)(mOutputBuffer->getBuffer()), inbuffer, 0 );
#else
	size_t writed =  ::write(mChannel, mOutputBuffer->getBuffer(), inbuffer);
#endif
	inbuffer = inbuffer - writed;
}

bool ClientDetails::readSocket() {
	int readCount = mBuffer->readFromFD(mChannel);
	if ( readCount < 1 ) {
		return true;
	} else {
		while ( process() );
	}
	return false;
}

StyxRAttachMessage* ClientDetails::processAttach(StyxTAttachMessage *msg) {
	std::string mountPoint = msg->getMountPoint();
	mClientRoot = mServerRoot; // FIXME later ->getDirectory(mountPoint);
	mUserName = msg->getUserName();
	StyxRAttachMessage *answer = new StyxRAttachMessage(msg->getTag(), mClientRoot->getQID());
	registerOpenedFile(msg->getFID(), mClientRoot );
	return answer;
}

void ClientDetails::registerOpenedFile(uint32_t fid, IVirtualStyxFile* file) {
	mAssignedFiles->insert(
			std::pair<uint32_t, IVirtualStyxFile*>(fid, file));
}
/**
 * Handle TWalk message from client
 * @param msg
 */
StyxMessage* ClientDetails::processWalk(StyxTWalkMessage* msg) {
	uint32_t fid = msg->getFID();
	map<uint32_t,IVirtualStyxFile*>::iterator iterator = mAssignedFiles->find(fid);
	if ( iterator == mAssignedFiles->end() ) {
		return getNoFIDError(msg, fid);
	}

	IVirtualStyxFile *walkFile;
	std::vector<StyxString*> *pathElementsArray = msg->getPathElements();
	std::vector<StyxQID*> *QIDList = new std::vector<StyxQID*>();

	if ( pathElementsArray->size() == 0 ) {
		walkFile = iterator->second;
	} else {
		walkFile = iterator->second->walk(
				pathElementsArray,
				QIDList);
	}

	if ( walkFile != NULL ) {
		mAssignedFiles->insert(
				std::pair<uint32_t, IVirtualStyxFile*>(msg->getNewFID(), walkFile));
		StyxRWalkMessage* result = new StyxRWalkMessage(msg->getTag(), QIDList);
		//		result->setDeleteQIDs(true);
		return result;
	} else {
		return new StyxRErrorMessage(msg->getTag(), "File does not exist");
	}
}
/**
 *
 * @param tag message tag
 * @param fid File ID
 * @return new Rerror message
 */
StyxRErrorMessage* ClientDetails::getNoFIDError(StyxMessage *message, StyxFID fid) {
	return new StyxRErrorMessage(message->getTag(),"Unknown FID (%d)"); // TODO add this , fid);
}
/**
 * Process incoming Tstat message
 */
StyxMessage* ClientDetails::processStat(StyxTStatMessage *msg) {
	map<uint32_t,IVirtualStyxFile*>::iterator iterator = mAssignedFiles->find(msg->getFID());
	if ( iterator == mAssignedFiles->end() ) {
		return getNoFIDError(msg, msg->getFID());
	}
	return new StyxRStatMessage(msg->getTag(), iterator->second->getStat());
}
/**
 * Handle TOpen message from client
 */
StyxMessage* ClientDetails::processTopen(StyxTOpenMessage *msg) {
	map<uint32_t,IVirtualStyxFile*>::iterator iterator = mAssignedFiles->find(msg->getFID());
	if ( iterator == mAssignedFiles->end() ) {
		return getNoFIDError(msg, msg->getFID());
	}
	if ( iterator->second->open(this, msg->getMode()) ) {
		return new StyxROpenMessage(msg->getTag(), iterator->second->getQID(), mIOUnit-24 ); // TODO magic number
	} else {
		return new StyxRErrorMessage(msg->getTag(), "Incorrect mode for specified file");
	}
}
/**
 * Handle read operation
 * @param msg
 */
StyxMessage* ClientDetails::processRead(StyxTReadMessage *msg) {
	if ( msg->getCount() > mIOUnit ) {
		return new StyxRErrorMessage(msg->getTag(), "IOUnit overflow");
	}
	map<uint32_t,IVirtualStyxFile*>::iterator iterator = mAssignedFiles->find(msg->getFID());
	if ( iterator == mAssignedFiles->end() ) {
		return getNoFIDError(msg, msg->getFID());
	}
	uint8_t *buffer = new uint8_t[msg->getCount()];
	size_t readed = iterator->second->read(this, buffer, msg->getOffset(), msg->getCount());
	return new StyxRReadMessage(msg->getTag(), buffer, readed);
}
/**
 * Handle clunk request
 * @param msg
 */
StyxMessage* ClientDetails::processClunk(StyxTCreateMessage *msg) {
	map<uint32_t,IVirtualStyxFile*>::iterator iterator = mAssignedFiles->find(msg->getFID());
	if ( iterator == mAssignedFiles->end() ) {
		return getNoFIDError(msg, msg->getFID());
	}
	iterator->second->close(this);
	mAssignedFiles->erase(iterator);
	return new StyxRClunkMessage(msg->getTag());
}
/**
 * Handle TWrite messages
 * @param msg
 */
StyxMessage* ClientDetails::processWrite(StyxTWriteMessage *msg) {
	//	printf("Got TWrite %dx%d\n", msg->getOffset(), msg->getCount());
	map<uint32_t,IVirtualStyxFile*>::iterator iterator = mAssignedFiles->find(msg->getFID());
	if ( iterator == mAssignedFiles->end() ) {
		return getNoFIDError(msg, msg->getFID());
	}
	size_t writed = iterator->second->write(this,
			msg->getData(),
			msg->getOffset(),
			msg->getCount());
	return new StyxRWriteMessage(msg->getTag(), writed);
}
/**
 * Handle TWStat messages
 * @param msg
 */
StyxMessage* ClientDetails::processWStat(StyxTWStatMessage *msg) {
	return new StyxRWStatMessage(msg->getTag());
}
