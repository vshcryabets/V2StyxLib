/*
 * ClientState.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "ClientState.h"

ClientState::ClientState(int iounit,
		Socket channel,
		IVirtualStyxDirectory *root) : mIOUnit(iounit), mChannel(channel), mServerRoot(root) {
	// TODO Auto-generated constructor stub

}

ClientState::~ClientState() {
	// TODO Auto-generated destructor stub
}

bool ClientState::read() {
	return false;
}

