/*
 * StyxTWalkMessage.cpp
 *
 *  Created on: Jun 2, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxTWalkMessage.h"
#include <sstream>

StyxTWalkMessage::StyxTWalkMessage(StyxFID fid, StyxFID new_fid)
	: StyxTMessageFID(Twalk, Rwalk, fid), mNewFID(new_fid) {
	mPathElements = std::vector<StyxString>();
}

StyxTWalkMessage::~StyxTWalkMessage() {
}

uint32_t StyxTWalkMessage::getNewFID() {
	return mNewFID;
}
std::vector<StyxString>* StyxTWalkMessage::getPathElements() {
	return &mPathElements; // TODO can be dangerous
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTWalkMessage::load(IStyxDataReader *input) {
	StyxTMessageFID::load(input);
	mNewFID = input->readUInt32();
	size_t count = input->readUInt16();
	for (size_t i=0; i<count; i++) {
		mPathElements.push_back(input->readUTFString());
	}
}
void StyxTWalkMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
	output->writeUInt32(mNewFID);
	output->writeUInt16(mPathElements.size());
	for ( std::vector<StyxString>::iterator it = mPathElements.begin();
			it != mPathElements.end();) {
		output->writeUTFString(*it);
	}
}
size_t StyxTWalkMessage::getBinarySize() {
	size_t size = StyxTMessageFID::getBinarySize() + 4 + 2;
	for ( std::vector<StyxString>::iterator it = mPathElements.begin();
			it != mPathElements.end();) {
			size += (*it).size()+2;
	}
	return size;
}

StyxString StyxTWalkMessage::getPath() {
	std::stringstream stream;
	for (std::vector<StyxString>::iterator it = mPathElements.begin();
		it < mPathElements.end(); ++it) {
			stream << "/" << *it;
	}
	return stream.str();
}