/*
 * StyxRSingleQIDMessage.h
 *
 *  Created on: Jan 21, 2016
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRSINGLEQIDMESSAGE_H_
#define STYXRSINGLEQIDMESSAGE_H_
#include "./types.h"
#include "./io/IStyxDataReader.h"
#include "./io/IStyxDataWriter.h"

class StyxRSingleQIDMessage : public StyxMessage {
private:
    StyxQID mQID;
    
public:
    StyxRSingleQIDMessage(MessageTypeEnum type, StyxTAG tag, StyxQID qid);
    virtual ~StyxRSingleQIDMessage();
};

#endif /* STYXRSINGLEQIDMESSAGE_H_ */
