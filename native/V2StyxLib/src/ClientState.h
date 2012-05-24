/*
 * ClientState.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef CLIENTSTATE_H_
#define CLIENTSTATE_H_
#include "types.h"
#include <string>
#include "classes.h"
#include <map>
#include "DualStateBuffer.h"

class ClientState {
private:
	std::string mUserName;
	DualStateBuffer *mBuffer;
	size_t mIOUnit;
	Socket mChannel;
	IVirtualStyxDirectory *mServerRoot;
	IVirtualStyxDirectory *mClientRoot;
	std::map<unsigned int32_t,IVirtualStyxFile*> *mAssignedFiles;

	bool process();
public:
	ClientState(size_t iounit,
			Socket channel,
			IVirtualStyxDirectory *root);
	~ClientState();
	bool readSocket();
};

#endif /* CLIENTSTATE_H_ */
