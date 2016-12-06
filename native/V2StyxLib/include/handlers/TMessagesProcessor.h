/*
 * TMessagesProcessor.h
 *
 *  Created on: Dec 5, 2016
 *      Author: vova
 */

#ifndef INCLUDE_HANDLERS_TMESSAGESPROCESSOR_H_
#define INCLUDE_HANDLERS_TMESSAGESPROCESSOR_H_

#include "types.h"
#include "vfs/IVirtualStyxFile.h"

class TMessagesProcessor {
protected:
	ConnectionDetails mConnectionDetails;
public:
	TMessagesProcessor(ConnectionDetails details, IVirtualStyxFile *root);
	virtual ~TMessagesProcessor();
};

#endif /* INCLUDE_HANDLERS_TMESSAGESPROCESSOR_H_ */
