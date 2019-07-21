/*
 * StyxRSingleQIDMessage.h
 *
 *  Created on: Jan 21, 2016
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRSINGLEQIDMESSAGE_H_
#define STYXRSINGLEQIDMESSAGE_H_
#include "types.h"
#include "io/IStyxDataReader.h"
#include "io/IStyxDataWriter.h"
#include "messages/base/StyxMessage.h"
#include "messages/base/structs/StyxQID.h"

class StyxRSingleQIDMessage : public StyxMessage {
private:
    StyxQID mQID;
public:
    StyxRSingleQIDMessage(MessageTypeEnum type, StyxTAG tag, const StyxQID qid);
    virtual ~StyxRSingleQIDMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();

    /**
     * @return QID structure
     */
    StyxQID getQID();
    virtual StyxString toString();
};

#endif /* STYXRSINGLEQIDMESSAGE_H_ */
