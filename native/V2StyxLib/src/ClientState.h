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

class ClientState;
#include "IVirtualStyxDirectory.h"

class ClientState {
private:
	std::string mUserName;
//	DualStateBuffer mBuffer;
	int mIOUnit;
	Socket mChannel;
//	StyxByteBuffer mOutputBuffer;
	IVirtualStyxDirectory *mServerRoot;
	IVirtualStyxDirectory *mClientRoot;
//	HashMap<Long, IVirtualStyxFile> mAssignedFiles;
public:
	ClientState(int iounit,
			Socket channel,
			IVirtualStyxDirectory *root);
	virtual ~ClientState();
	bool read();
};

#endif /* CLIENTSTATE_H_ */
