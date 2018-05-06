/*
 * TMessagesProcessor.cpp
 *
 *  Created on: Dec 5, 2016
 *      Author: vova
 */

#include "handlers/TMessagesProcessor.h"
#include "messages/StyxRVersionMessage.h"
#include "messages/StyxRAttachMessage.h"
#include "messages/StyxRStatMessage.h"
#include "messages/StyxROpenMessage.h"
#include "messages/StyxRReadMessage.h"
#include "messages/StyxRWalkMessage.h"
#include "messages/StyxRWriteMessage.h"
#include "messages/StyxRAuthMessage.h"

TMessagesProcessor::TMessagesProcessor(StyxString tag, ConnectionDetails details, IVirtualStyxFile *root)
	: QueueMessagesProcessor(tag), mConnectionDetails(details), mRoot(root), mHandledPackets(0), 
	mErrorPackets(0), mAnswerPackets(0) {
}

TMessagesProcessor::~TMessagesProcessor() {
	// TODO Auto-generated destructor stub
}

void TMessagesProcessor::addClient(ClientDetails *state) {
	mRoot->onConnectionOpened(state);
}

void TMessagesProcessor::removeClient(ClientDetails *state) {
	mRoot->onConnectionClosed(state);
}

void TMessagesProcessor::processPacket(StyxMessage *message, ClientDetails *target) throw(StyxException) {
	mHandledPackets++;
	StyxMessage* answer = NULL;
	IVirtualStyxFile* file;
	StyxFID fid;
	try {
		switch (message->getType()) {
			case Tversion:
				answer = new StyxRVersionMessage(mConnectionDetails.getIOUnit(),
						mConnectionDetails.getProtocol());
				break;
			case Tattach:
				answer = processAttach(target, (StyxTAttachMessage*)message);
				break;
			case Tauth:
				answer = processAuth(target, (StyxTAuthMessage*)message);
				break;
			case Tstat:
				fid = ((StyxTMessageFID*)message)->getFID();
				file = target->getAssignedFile(fid);
				answer = new StyxRStatMessage(message->getTag(), file->getStat());
				// TODO release file?
				break;
			case Tclunk:
				answer = processClunk(target, (StyxTMessageFID*)message);
				break;
			case Tflush:
				// TODO do something there
				answer = new StyxMessage(Rflush, message->getTag());
				break;
			case Twalk:
				answer = processWalk(target, (StyxTWalkMessage*) message);
				break;
			case Topen:
				answer = processOpen(target, (StyxTOpenMessage*)message);
				break;
			case Tread:
				answer = processRead(target, (StyxTReadMessage*)message);
				break;
			case Twrite:
				answer = processWrite(target, (StyxTWriteMessage*)message);
				break;
			case Twstat:
				answer = processWStat(target, (StyxTWStatMessage*)message);
				break;
			case Tcreate:
				answer = processCreate(target, (StyxTCreateMessage*)message);
				break;
			case Tremove:
				answer = processRemove(target, (StyxTMessageFID*)message);
				break;
			default:
#warning TODO handle this
				printf("Got unsupported message: %s", message->toString().c_str());
				break;
		}
	} catch (StyxErrorMessageException e) {
		answer = e.constructErrorMessage();
		answer->setTag(message->getTag());
		mErrorPackets++;
	}
	if ( answer != NULL ) {
		mAnswerPackets++;
		target->getDriver()->sendMessage(answer, target);
	}
}

StyxMessage* TMessagesProcessor::processAuth(ClientDetails* clientDetails, StyxTAuthMessage* msg) {
	Credentials credentials(msg->getUserName(), "");
	clientDetails->setCredentials(credentials);
	// TODO handle auth packet
	return new StyxRAuthMessage(msg->getTag(), StyxQID::EMPTY);
}

StyxMessage* TMessagesProcessor::processOpen(ClientDetails* clientDetails, StyxTOpenMessage* msg) throw (StyxErrorMessageException) {
	IVirtualStyxFile* file = clientDetails->getAssignedFile(msg->getFID());
	if (file->open(clientDetails, msg->getMode())) {
		return new StyxROpenMessage(msg->getTag(), file->getQID(),
				mConnectionDetails.getIOUnit() - DEFAULT_PACKET_HEADER_SIZE, false);
	} else {
		throw StyxErrorMessageException("Not supported mode for specified file");
	}
}

StyxMessage* TMessagesProcessor::processRead(ClientDetails* clientDetails, StyxTReadMessage* msg) throw(StyxErrorMessageException) {
	if (msg->getCount() > mConnectionDetails.getIOUnit()) {
		throw StyxErrorMessageException("IOUnit overflow");
	}
	StyxFID fid = msg->getFID();
	IVirtualStyxFile* file = clientDetails->getAssignedFile(fid);
	StyxBuffer buffer(msg->getCount());
	size_t read = file->read(clientDetails, buffer.data(), msg->getOffset(), msg->getCount());
	return new StyxRReadMessage(msg->getTag(), buffer, read);
}

StyxMessage* TMessagesProcessor::processWalk(ClientDetails* clientDetails, 
	StyxTWalkMessage* msg) throw(StyxErrorMessageException) {
	StyxFID fid = msg->getFID();
	std::vector<StyxQID> QIDList;
	IVirtualStyxFile* walkFile = clientDetails->getAssignedFile(fid)->walk(
			msg->getPathElements(),
			&QIDList);
	if (walkFile != NULL) {
		clientDetails->registerOpenedFile(msg->getNewFID(), walkFile);
		return new StyxRWalkMessage(msg->getTag(), QIDList);
	} else {
		throw StyxErrorMessageException("file \"%s\" does not exist", msg->getPath().c_str());
	}
}

StyxMessage* TMessagesProcessor::processClunk(ClientDetails* clientDetails, 
	StyxTMessageFID* msg) throw(StyxErrorMessageException) {
	clientDetails->getAssignedFile(msg->getFID())->close(clientDetails);
	clientDetails->unregisterClosedFile(msg->getFID());
	return new StyxMessage(Rclunk, msg->getTag());
}

StyxMessage* TMessagesProcessor::processWStat(ClientDetails* clientDetails, 
	StyxTWStatMessage* msg) throw(StyxErrorMessageException) {
	// TODO handle Twstat
	return new StyxMessage(Rwstat, msg->getTag());
}

StyxMessage* TMessagesProcessor::processWrite(ClientDetails* clientDetails, 
	StyxTWriteMessage* msg) throw(StyxErrorMessageException) {
	IVirtualStyxFile* file = clientDetails->getAssignedFile(msg->getFID());
	size_t count = file->write(clientDetails, msg->getData(), msg->getOffset(), msg->getCount());
	return new StyxRWriteMessage(msg->getTag(), count);
}

StyxMessage* TMessagesProcessor::processAttach(ClientDetails* clientDetails, 
	StyxTAttachMessage* msg) {
	Credentials credentials(msg->getUserName(), "");
	clientDetails->setCredentials(credentials);
	StyxString mountPoint = msg->getMountPoint();
	IVirtualStyxFile* root = mRoot; // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
	StyxRAttachMessage* answer = new StyxRAttachMessage(msg->getTag(), root->getQID());
	clientDetails->registerOpenedFile(msg->getFID(), root);
	return answer;
}

StyxMessage* TMessagesProcessor::processCreate(ClientDetails* clientDetails, 
	StyxTCreateMessage* msg) throw(StyxErrorMessageException) {
	IVirtualStyxFile* file = clientDetails->getAssignedFile(msg->getFID());
	StyxQID qid = file->createFile(msg->getName(), msg->getPermissions(), msg->getMode());
	return new StyxROpenMessage(msg->getTag(), qid, mConnectionDetails.getIOUnit(), true);
}

StyxMessage* TMessagesProcessor::processRemove(ClientDetails* clientDetails, 
	StyxTMessageFID* msg) throw(StyxErrorMessageException) {
	clientDetails->getAssignedFile(msg->getFID())->deleteFile(clientDetails);
	return new StyxMessage(Rremove, msg->getTag());
}


size_t TMessagesProcessor::getReceivedPacketsCount() {
	return mHandledPackets;
}

size_t TMessagesProcessor::getReceivedErrorPacketsCount() {
	return mErrorPackets;
}
