/*
 * StyxRWalkMessage.h
 *
 *  Created on: Jun 2, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRWALKMESSAGE_H_
#define STYXRWALKMESSAGE_H_

#include "StyxMessage.h"
#include <vector>

class StyxRWalkMessage: public StyxMessage {
private:
	std::vector<StyxQID*> *mQIDList;
	bool mDelete;
public:
	StyxRWalkMessage(StyxTAG tag, std::vector<StyxQID*> *QIDList);
	virtual ~StyxRWalkMessage();
	void setDeleteQIDs(bool value);
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRWALKMESSAGE_H_ */
