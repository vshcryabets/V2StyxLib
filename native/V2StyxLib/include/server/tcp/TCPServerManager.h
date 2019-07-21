/*
 * TCPServerManager.h
 *
 *  Created on: Dec 6, 2016
 *      Author: vova
 */

#ifndef INCLUDE_SERVER_TCP_TCPSERVERMANAGER_H_
#define INCLUDE_SERVER_TCP_TCPSERVERMANAGER_H_

#include "library/StyxServerManager.h"
#include "server/tcp/TCPChannelDriver.h"

#warning this class looks useless, probably we can remove it
class TCPServerManager: public StyxServerManager {
private:
	TCPChannelDriver *mDriver;
public:
	TCPServerManager(StyxString address, uint16_t port, IVirtualStyxFile* root);
	virtual ~TCPServerManager();
};

#endif /* INCLUDE_SERVER_TCP_TCPSERVERMANAGER_H_ */
