/*
 * TCPChannelDriver.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_
#define INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_

#include "server/IChannelDriver.h"

class TCPChannelDriver : public IChannelDriver {
protected:
	uint16_t mPort;
	StyxString mAddress;
	uint32_t mTransmittedPacketsCount;
	uint32_t mTransmissionErrorsCount;
    IMessageProcessor* mTMessageHandler;
    IMessageProcessor* mRMessageHandler;
    uint32_t mIOUnit;
    bool isWorking;
    StyxThread mAcceptorThread;

#ifdef USE_LOGGING
    ILogListener* mLogListener;
#endif

	virtual void run() = 0;
	virtual void prepareSocket(StyxString socketAddress, uint16_t port, bool ssl) throw(StyxException) = 0;

	// get connection timeout in miliseconds
	virtual size_t getTimeout();
public:
	TCPChannelDriver(StyxString address, uint16_t port, bool ssl);
	virtual ~TCPChannelDriver();
	virtual StyxThread start(size_t iounit);
	virtual bool sendMessage(StyxMessage* message, ClientDetails *recipient) throw(StyxException);
	virtual void setTMessageHandler(IMessageProcessor *handler);
	virtual void setRMessageHandler(IMessageProcessor *handler);
	virtual void close() throw(StyxException);
	virtual size_t getTransmittedCount();
	virtual size_t getErrorsCount();
	virtual void setLogListener(ILogListener *listener);
	virtual IMessageProcessor* getTMessageHandler();
	virtual IMessageProcessor* getRMessageHandler();
    uint16_t getPort();
};

#endif /* INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_ */
