/*
 * Connection.h
 *
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CONNECTION_H_
#define CONNECTION_H_

#include "types.h"
#include "IClient.h"
#include "handlers/RMessagesProcessor.h"
#include "handlers/TMessagesProcessor.h"
#include "handlers/TMessageTransmitter.h"
#include "messages/base/structs/StyxQID.h"

/**
 * Styx client connection.
 */
class Connection : public IClient {
private:
	static const StyxString PROTOCOL;
	static const size_t DEFAULT_IOUNIT;
	static const size_t DEFAULT_TIMEOUT;

	IVirtualStyxFile *mRoot;
    StyxString mMountPoint;
    size_t mTimeout;
    bool isConnectedFlag;
    bool isAttached;
    TMessageTransmitter* mTransmitter;
    StyxFID mAuthFID;
    StyxQID* mAuthQID;
    StyxQID* mQID;
    StyxFID mFID;


	void setAddress(const char * hname,
			short port,
			struct sockaddr_in * sap,
			char* protocol);
	// create and bind socket
	Socket createSocket(string address, int port);
	void init(IChannelDriver *driver,
            RMessagesProcessor *answerProcessor,
            TMessageTransmitter *transmitter,
            ClientDetails *recepient);
protected:
    Credentials mCredentials;
    ClientDetails* mRecepient;
    IChannelDriver* mDriver;
    ConnectionDetails* mDetails;
    RMessagesProcessor* mAnswerProcessor;
    bool isAutoStartDriver;
    bool shouldCloseAnswerProcessor;
    bool shouldCloseTransmitter;

    virtual size_t getIOBufSize();

public:
    Connection();
    Connection(Credentials credentials);
    Connection(Credentials credentials, IChannelDriver *driver);
    Connection(Credentials credentials,
                      IChannelDriver *driver,
                      RMessagesProcessor *answerProcessor,
                      TMessageTransmitter *transmitter,
                      ClientDetails *recepient);
    virtual ~Connection();

	virtual void sendVersionMessage() throw();
	virtual bool connect(IChannelDriver *driver) throw();
	virtual bool connect(IChannelDriver *driver, Credentials credentials) throw();
	virtual bool connect() throw();
	virtual bool isConnected();
	virtual IMessageTransmitter *getMessenger();
	virtual size_t getTimeout();
	virtual StyxFID getRootFID();
	virtual ConnectionDetails getConnectionDetails();
	virtual ClientDetails *getRecepient();
	virtual void close() throw();
	virtual StyxString getProtocol();
};

#endif /* CONNECTION_H_ */
