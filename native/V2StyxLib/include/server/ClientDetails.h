/*
 * ClientState.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef CLIENTSTATE_H_
#define CLIENTSTATE_H_
#include "../types.h"
#include <string>
#include "../classes.h"
#include <map>
#include "messages/base/StyxMessage.h"
#include "messages/StyxTAttachMessage.h"
#include "messages/StyxRAttachMessage.h"
#include "messages/StyxTWalkMessage.h"
#include "messages/StyxRWalkMessage.h"
#include "messages/StyxRErrorMessage.h"
#include "messages/StyxTOpenMessage.h"
#include "messages/StyxTReadMessage.h"
#include "messages/StyxTWriteMessage.h"
#include "messages/StyxTWStatMessage.h"
#include "io/StyxByteBufferReadable.h"
#include "io/StyxByteBufferWritable.h"
#include "messages/StyxTCreateMessage.h"
#include "vfs/IVirtualStyxFile.h"

class ClientDetails {
private:
protected:
	Credentials *mCredentials;
	StyxByteBufferReadable *mBuffer;
	StyxByteBufferWritable *mOutputBuffer;
	size_t mIOUnit;
	Socket mChannel;
	IVirtualStyxFile *mServerRoot;
	IVirtualStyxFile *mClientRoot;
	std::map<StyxFID,IVirtualStyxFile*> *mAssignedFiles;

	bool process();
	/**
	 * Processing incoming messages
	 * @param msg incomming message
	 */
	void processMessage(StyxMessage *msg);
	/**
	 * Send answer message to client
	 * @param answer
	 */
	void sendMessage(StyxMessage *answer);
	/**
	 * Process incoming Tattach message
	 */
	StyxRAttachMessage* processAttach(StyxTAttachMessage *msg);
	/**
	 * Process incoming Tstat message
	 */
	StyxMessage* processStat(StyxTStatMessage *msg);
	/**
	 * Handle TWalk message from client
	 * @param msg
	 */
	StyxMessage* processWalk(StyxTWalkMessage* msg);
	/**
	 * Handle TOpen message from client
	 */
	StyxMessage* processTopen(StyxTOpenMessage *msg);
	/**
	 * Handle read operation
	 * @param msg request message
	 */
	StyxMessage* processRead(StyxTReadMessage *msg);
	/**
	 * Handle clunk request
	 */
	StyxMessage* processClunk(StyxTCreateMessage *msg);
	/**
	 * Handle Write request
	 */
	StyxMessage* processWrite(StyxTWriteMessage *msg);
	/**
	 * Handle TWStat messages
	 * @param msg
	 */
	StyxMessage* processWStat(StyxTWStatMessage *msg);
	void registerOpenedFile(uint32_t fid, IVirtualStyxFile* file);
	StyxRErrorMessage* getNoFIDError(StyxMessage* message, StyxFID fid);
public:
	ClientDetails(size_t iounit,
			Socket channel,
			IVirtualStyxFile *root,
			std::string protocol);
	~ClientDetails();
	bool readSocket();
};

#endif /* CLIENTSTATE_H_ */
