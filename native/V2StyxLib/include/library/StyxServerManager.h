/*
 * StyxServerManager.h
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef STYXSERVERMANAGER_H_
#define STYXSERVERMANAGER_H_
#include <string>
#include "types.h"
#include "classes.h"
#include <vector>
#include "server/IChannelDriver.h"
#include "handlers/TMessagesProcessor.h"

class StyxServerManager {
public:
	static const StyxString PROTOCOL;
	static const size_t DEFAULT_IOUNIT;
protected:
	std::vector<IChannelDriver*> mDrivers;
	TMessagesProcessor *mBalancer; // TODO can we remove pointer?
	ConnectionAcceptor *mAcceptor;
	IVirtualStyxFile *mRoot;
	std::vector<StyxThread*> mDriverThreads;

public:
	static const size_t DEFAULT_TIMEOUT;

	StyxServerManager(IVirtualStyxFile *root, std::vector<IChannelDriver*> drivers = std::vector<IChannelDriver*>());
	~StyxServerManager();

	virtual StyxServerManager* addDriver(IChannelDriver* driver) throw(StyxException);

	/**
	 * start server
	 */
	virtual std::vector<StyxThread*> start();
	/**
	 * stop server and realease all resources
	 */
	virtual void close();

	/**
	 * stop server and realease all resources
	 */
	virtual void closeAndWait();

	/**
	* Set non-blocking mode
	*/
	static void setNonBlocking(Socket socket);

	/**
     * Get supported protocol name.
     * @return supported protocol name.
     */
    virtual StyxString getProtocol();

    /**
     * Get supported IO unit size.
     * @return supported IO unit size.
     */
    virtual size_t getIOUnit();
    virtual IVirtualStyxFile* getRoot();
    virtual std::vector<IChannelDriver*> getDrivers();
};

#endif /* STYXSERVERMANAGER_H_ */
