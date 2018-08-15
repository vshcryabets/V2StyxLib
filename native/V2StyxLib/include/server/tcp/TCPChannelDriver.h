/*
 * TCPChannelDriver.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_
#define INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_

#include "server/IChannelDriver.h"
#include "server/tcp/TCPClientDetails.h"

class TCPChannelDriver : public IChannelDriver, IRunnable {
protected:
	uint16_t mPort;
	StyxString mAddress;
	uint32_t mTransmittedPacketsCount;
	uint32_t mTransmissionErrorsCount;
    IMessageProcessor* mTMessageHandler;
    IMessageProcessor* mRMessageHandler;
    uint32_t mIOUnit;
    bool isWorking;
#warning rename to mThread
    StyxThread* mAcceptorThread;

#ifdef USE_LOGGING
    ILogListener* mLogListener;
#endif


	// get connection timeout in miliseconds
	virtual size_t getTimeout();
	virtual StyxMessage* parseMessage(IStyxDataReader* reader) throw(StyxException);
	virtual bool readSocket(TCPClientDetails* client) throw(StyxException);
	/**
     * Read income message from specified client.
     * @return true if message was processed
     * @throws StyxException in case of parse error.
     */
    virtual bool process(TCPClientDetails* client) throw(StyxException);

public:
	TCPChannelDriver(StyxString address, uint16_t port);
	virtual ~TCPChannelDriver();
	virtual StyxThread* start(size_t iounit) throw(StyxException);
	virtual bool sendMessage(StyxMessage* message, ClientDetails *recipient) throw(StyxException);
	virtual void setTMessageHandler(IMessageProcessor *handler);
	virtual void setRMessageHandler(IMessageProcessor *handler);
	virtual void close() throw(StyxException);
	virtual size_t getTransmittedCount();
	virtual size_t getErrorsCount();
	virtual void prepareSocket() throw(StyxException) = 0;
	virtual void closeSocket() throw(StyxException) = 0;
#ifdef USE_LOGGING
	virtual void setLogListener(ILogListener *listener);
#endif
	virtual IMessageProcessor* getTMessageHandler();
	virtual IMessageProcessor* getRMessageHandler();
    uint16_t getPort();
};

#endif /* INCLUDE_SERVER_TCP_TCPCHANNELDRIVER_H_ */
