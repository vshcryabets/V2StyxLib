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

TMessagesProcessor::TMessagesProcessor(ConnectionDetails details, IVirtualStyxFile *root)
	: mConnectionDetails(details), mRoot(root), mHandledPackets(0), mErrorPackets(0), mAnswerPackets(0) {
}

TMessagesProcessor::~TMessagesProcessor() {
	// TODO Auto-generated destructor stub
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
				// TODO handle this
				//System.out.println("Got message:");
				//System.out.println(message.toString());
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
	StyxString mountPoint = msg->getMountPoint();
	IVirtualStyxFile* root = mRoot; // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
	StyxRAttachMessage* answer = new StyxRAttachMessage(msg->getTag(), root->getQID());
	clientDetails->registerOpenedFile(msg->getFID(), root);
	return answer;
}