/*
 * TCPServerManager.h
 *
 *  Created on: Dec 6, 2016
 *      Author: vova
 */

#ifndef INCLUDE_SERVER_TCP_TCPSERVERMANAGER_H_
#define INCLUDE_SERVER_TCP_TCPSERVERMANAGER_H_

#include "StyxServerManager.h"

class TCPServerManager: public StyxServerManager {
public:
	TCPServerManager(StyxString address, uint16_t port, bool ssl, IVirtualStyxFile* root);
	virtual ~TCPServerManager();
};

#endif /* INCLUDE_SERVER_TCP_TCPSERVERMANAGER_H_ */
