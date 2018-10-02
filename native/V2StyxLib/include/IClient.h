/*
 * IClient.h
 *
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */
#ifndef ICLIENT_H_
#define ICLIENT_H_

#include "server/IChannelDriver.h"

class IClient {
public:
	IClient() {};
	virtual ~IClient() {};

	/**
	 * Connect to server.
	 * @return true if connected
	 * @throws java.io.IOException
	 * @throws com.v2soft.styxlib.exceptions.StyxException
	 * @throws java.util.concurrent.TimeoutException
	 */
	virtual bool connect() throw(StyxException) = 0;

	virtual bool isConnected() = 0;

	virtual IMessageTransmitter *getTransmitter() = 0;

	virtual size_t getTimeout() = 0;
	/**
	 *
	 * @return FID of root folder
	 */
	virtual StyxFID getRootFID() = 0;

	virtual ConnectionDetails getConnectionDetails() = 0;

	/**
	 *
	 * @return message recepient information
	 */
	virtual ClientDetails *getRecepient()  = 0;

	/**
	 * Close connection to server.
	 */
	virtual void close() throw(StyxException) = 0;
};

#endif // ICLIENT_H_
