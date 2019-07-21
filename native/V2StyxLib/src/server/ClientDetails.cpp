/*
 * ClientState.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "server/ClientDetails.h"
#include "stdio.h"
#include "messages/base/StyxMessage.h"
#include "exceptions/StyxErrorMessageException.h"
#include <vector>
#include <unistd.h>
#include <sstream>

ClientDetails::ClientDetails(IChannelDriver* driver, uint32_t iounit, uint32_t id) 
	: mDriver(driver), mClientId(id), mCredentials("", ""),
	mOutputBuffer(StyxBuffer(iounit)), mOutputWriter(&mOutputBuffer),
	mInputBuffer(StyxByteBufferReadable(iounit * 2)), mInputReader(StyxDataReader(&mInputBuffer)) {
	if (mDriver == NULL) {
		throw StyxException("Driver is null");
	}
}

ClientDetails::~ClientDetails() {
}

void ClientDetails::registerOpenedFile(StyxFID fid, IVirtualStyxFile* file) {
	mAssignedFiles.insert(std::pair<StyxFID, IVirtualStyxFile*>(fid, file));
}

StyxString ClientDetails::toString() {
	std::stringstream stream;
	stream << "id " << mClientId;
	if ( mDriver != NULL ) {
		stream << " driver " << mDriver->toString();
	}
	return stream.str();
}

IVirtualStyxFile* ClientDetails::getAssignedFile(StyxFID fid) throw(StyxErrorMessageException) {
	std::map<StyxFID, IVirtualStyxFile*>::iterator it = mAssignedFiles.find(fid);
	if (it == mAssignedFiles.end() ) {
		throw StyxErrorMessageException("Unknown FID (%d)", fid);
	}
	return it->second;
}

IChannelDriver* ClientDetails::getDriver() {
	return mDriver;
}

void ClientDetails::setCredentials(Credentials credentials) {
	mCredentials = credentials;
}

void ClientDetails::unregisterClosedFile(StyxFID fid) {
	std::map<StyxFID, IVirtualStyxFile*>::iterator it = mAssignedFiles.find(fid);
	if (it == mAssignedFiles.end()) {
		throw StyxErrorMessageException("Unknown FID (%d)", fid);
	}
	mAssignedFiles.erase(it);
}

StyxDataWriter* ClientDetails::getOutputWritter() {
	return &mOutputWriter;
}

StyxBuffer* ClientDetails::getOutputBuffer() {
	return &mOutputBuffer;
}

StyxByteBufferReadable* ClientDetails::getInputBuffer() {
	return &mInputBuffer;
}

IStyxDataReader* ClientDetails::getInputReader() {
	return &mInputReader;
}

void ClientDetails::releaseFID(StyxTMessageFID* message) 
{
	mFids.release(message->getFID());
}

StyxTMessage* ClientDetails::findTMessageAndRelease(StyxTAG tag) 
{
	std::map<StyxTAG, StyxTMessage*>::iterator it = mMessagesMap.find(tag);
	if (it == mMessagesMap.end()) {
		return NULL;
	}
	StyxTMessage* result = it->second;
	mMessagesMap.erase(tag);
	mTags.release(tag);
	return result;
}

MessageTagPoll* ClientDetails::getTagPoll() 
{
	return &mTags;
}

void ClientDetails::putTMessage(StyxTAG tag, StyxTMessage* message) 
{
	mMessagesMap[tag] = message;
}

FIDPoll* ClientDetails::getFIDPoll() 
{
	return &mFids;
}