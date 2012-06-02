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
#include "../messages/StyxMessage.h"
#include "../messages/StyxTAttachMessage.h"
#include "../messages/StyxRAttachMessage.h"
#include "../messages/StyxTWalkMessage.h"
#include "../messages/StyxRWalkMessage.h"
#include "../messages/StyxRErrorMessage.h"
#include "../io/StyxByteBufferReadable.h"
#include "../io/StyxByteBufferWritable.h"
#include "../vfs/IVirtualStyxDirectory.h"

class ClientState {
private:
	std::string *mUserName;
	std::string *mProtocol;
	StyxByteBufferReadable *mBuffer;
	StyxByteBufferWritable *mOutputBuffer;
	size_t mIOUnit;
	Socket mChannel;
	IVirtualStyxDirectory *mServerRoot;
	IVirtualStyxDirectory *mClientRoot;
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
	 * Handle TWalk message from client
	 * @param msg
	 */
	StyxMessage* processWalk(StyxTWalkMessage* msg);
	void registerOpenedFile(uint32_t fid, IVirtualStyxFile* file);
	StyxRErrorMessage* getNoFIDError(StyxMessage* message, StyxFID fid);
public:
	ClientState(size_t iounit,
			Socket channel,
			IVirtualStyxDirectory *root,
			std::string *protocol);
	~ClientState();
	bool readSocket();
};

#endif /* CLIENTSTATE_H_ */
