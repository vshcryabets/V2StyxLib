/*
 * Connection.h
 *
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CONNECTION_H_
#define CONNECTION_H_

#include "IClient.h"
#include "handlers/RMessagesProcessor.h"
#include "handlers/TMessageTransmitter.h"


/**
 * Styx client connection.
 */
class Connection : public IClient {
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
    Connection();
    Connection(Credentials credentials);
    Connection(Credentials credentials, IChannelDriver *driver);
    Connection(Credentials *credentials,
                      IChannelDriver *driver,
                      RMessagesProcessor *answerProcessor,
                      TMessageTransmitter *transmitter,
                      ClientDetails *recepient);
    ~Connection();

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
};

#endif /* STYXSERVERMANAGER_H_ */
