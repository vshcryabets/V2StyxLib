/*
 * ILogListener.h
 *
 *  Created on: Dec 3, 2016
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */
#ifndef ILOGLISTENER_H_
#define ILOGLISTENER_H_

#include "server/IChannelDriver.h"
#include "exceptions/StyxException.h"
#include "messages/base/StyxMessage.h"

class IChannelDriver;

class ILogListener {
public:
	virtual void onMessageReceived(IChannelDriver* driver,
			ClientDetails* clientDetails, StyxMessage* message) = 0;
	virtual void onMessageTransmited(IChannelDriver* driver,
			ClientDetails* clientDetails, StyxMessage* message) = 0;
	virtual void onException(IChannelDriver* driver, StyxException *error) = 0;
};

#endif // ILOGLISTENER_H_
