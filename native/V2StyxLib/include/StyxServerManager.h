/*
 * StyxServerManager.h
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef STYXSERVERMANAGER_H_
#define STYXSERVERMANAGER_H_
#include <string>
#ifdef WIN32
#include <WinSock2.h>
#else
/* According to POSIX.1-2001 */
#include <sys/select.h>
#endif

#include "types.h"
#include "classes.h"
#include "handlers/TMessagesProcessor.h"
#include "server/IChannelDriver.h"

#include <vector>
using namespace std;

class StyxServerManager {
private:
	static const StyxString PROTOCOL;
	static const size_t DEFAULT_IOUNIT;

	std::vector<IChannelDriver*> mDrivers;
	TMessagesProcessor *mBalancer;
	ConnectionAcceptor *mAcceptor;
	IVirtualStyxFile *mRoot;

	void setAddress(const char * hname,
			short port,
			struct sockaddr_in * sap,
			char* protocol);
	// create and bind socket
	Socket createSocket(string address, int port);
public:
	StyxServerManager(IVirtualStyxFile *root);
	~StyxServerManager();
	/**
	 * start server
	 */
	void start();
	/**
	 * stop server and realease all resources
	 */
	void stop();
	/**
	* Set non-blocking mode
	*/
	static void setNonBlocking(Socket socket);

	/**
     * Get supported protocol name.
     * @return supported protocol name.
     */
    StyxString getProtocol() {
        return PROTOCOL;
    }

    /**
     * Get supported IO unit size.
     * @return supported IO unit size.
     */
    size_t getIOUnit() {
        return DEFAULT_IOUNIT;
    }
};

#endif /* STYXSERVERMANAGER_H_ */
