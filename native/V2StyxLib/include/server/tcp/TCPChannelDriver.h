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
    ILogListener* mLogListener;

	virtual void run() = 0;
	virtual void prepareSocket(std::string socketAddress, bool ssl) throw() = 0;
public:
	TCPChannelDriver(StyxString address, uint16_t port, bool ssl);
	virtual ~TCPChannelDriver();
	virtual StyxThread start(int iounit);
	virtual bool sendMessage(StyxMessage message, ClientDetails *recipient) throw();
	virtual void setTMessageHandler(IMessageProcessor *handler);
	virtual void setRMessageHandler(IMessageProcessor *handler);
	virtual void close() throw();
	virtual size_t getTransmittedCount();
	virtual size_t getErrorsCount();
	virtual void setLogListener(ILogListener *listener);
	virtual IMessageProcessor* getTMessageHandler();
	virtual IMessageProcessor* getRMessageHandler();
    uint16_t getPort();
};

#endif /* INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_ */
