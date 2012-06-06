/*
 * StyxTWalkMessage.cpp
 *
 *  Created on: Jun 2, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxTWalkMessage.h"

StyxTWalkMessage::StyxTWalkMessage(StyxFID fid, StyxFID new_fid)
: StyxMessage(Twalk, NOTAG){
	mPathElements = new std::vector<StyxString*>();
}

StyxTWalkMessage::~StyxTWalkMessage() {
	for ( std::vector<StyxString*>::iterator it = mPathElements->begin();
			it != mPathElements->end();) {
		delete * it;
		it = mPathElements->erase(it);
	}
	delete mPathElements;
}

uint32_t StyxTWalkMessage::getFID() {
	return mFID;
}
uint32_t StyxTWalkMessage::getNewFID() {
	return mNewFID;
}
std::vector<StyxString*>* StyxTWalkMessage::getPathElements() {
	return mPathElements;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTWalkMessage::load(IStyxDataReader *input) {
	mFID = input->readUInt32();
	mNewFID = input->readUInt32();
	size_t count = input->readUInt16();
	for (size_t i=0; i<count; i++) {
		mPathElements->push_back(new StyxString(input->readUTFString()));
	}
}
size_t StyxTWalkMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mFID);
	output->writeUInt32(mNewFID);
	output->writeUInt16(mPathElements->size());
	for ( std::vector<StyxString*>::iterator it = mPathElements->begin();
			it != mPathElements->end();) {
		output->writeUTFString(*it);
	}
	return getBinarySize();
}
size_t StyxTWalkMessage::getBinarySize() {
	size_t size = StyxMessage::getBinarySize() + 10;
	for ( std::vector<StyxString*>::iterator it = mPathElements->begin();
			it != mPathElements->end();) {
			size += (*it)->size()+2;
	}
	return size;
}
