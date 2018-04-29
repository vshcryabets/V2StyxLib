/*
 * StyxRWalkMessage.cpp
 *
 *  Created on: Jun 2, 2012
 *      Author: vshcryabets@gmail.com
 */

#include "messages/StyxRWalkMessage.h"
#include "messages/base/structs/StyxQID.h"

StyxRWalkMessage::StyxRWalkMessage(StyxTAG tag, std::vector<StyxQID*> *QIDList)
: StyxMessage(Rwalk, tag) {
	mQIDList = QIDList;
	mDelete = false;
}

StyxRWalkMessage::~StyxRWalkMessage() {
	if ( mDelete ) {
		for ( std::vector<StyxQID*>::iterator it = mQIDList->begin();
				it != mQIDList->end();) {
			delete * it;
			it = mQIDList->erase(it);
		}
		delete mQIDList;
	}
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRWalkMessage::load(IStyxDataReader *buffer) {
	mQIDList = new std::vector<StyxQID*>();
	mDelete = true;
	int count = buffer->readUInt16();
	for (int i=0; i<count; i++) {
		mQIDList->push_back(new StyxQID(buffer));
	}
}
void StyxRWalkMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt16(mQIDList->size());
	for ( std::vector<StyxQID*>::iterator it = mQIDList->begin(); it != mQIDList->end(); it++ ) {
		(*it)->writeBinaryTo(output);
	}
}
size_t StyxRWalkMessage::getBinarySize() {
	size_t size = StyxMessage::getBinarySize() + 2
			+ mQIDList->size() * StyxQID::CONTENT_SIZE;
	return size;
}
void StyxRWalkMessage::setDeleteQIDs(bool value) {
	mDelete = value;
}
