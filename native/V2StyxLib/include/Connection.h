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
#include "server/ClientDetails.h"

/**
 * Styx client connection.
 */
class Connection : public IClient, TMessageTransmitter::Listener {
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
    StyxFID mFID;
    StyxQID mAuthQID;
    StyxQID mQID;

	void init(RMessagesProcessor *answerProcessor,
            TMessageTransmitter *transmitter,
            ClientDetails *recepient);
protected:
    Credentials mCredentials;
    ClientDetails* mRecepient;
    IChannelDriver* mDriver;
    ConnectionDetails mDetails;
    RMessagesProcessor* mAnswerProcessor;
    bool isAutoStartDriver;
    bool shouldCloseAnswerProcessor;
    bool shouldCloseTransmitter;

    virtual size_t getIOBufSize();
    virtual void sendAuthMessage() throw();
    virtual void sendAttachMessage() throw();
    virtual void setDriver(IChannelDriver* driver);
public:
    Connection(Credentials credentials = Credentials(NULL, NULL), IChannelDriver *driver = NULL);
    Connection(Credentials credentials,
                      IChannelDriver *driver,
                      RMessagesProcessor *answerProcessor,
                      TMessageTransmitter *transmitter,
                      ClientDetails *recepient);
    virtual ~Connection();

	virtual void sendVersionMessage() throw(StyxException);
	virtual bool connect() throw(StyxException);
	virtual bool connect(IChannelDriver *driver) throw(StyxException);
	virtual bool connect(IChannelDriver *driver, Credentials credentials) throw(StyxException);
	virtual bool connect(IChannelDriver *driver, Credentials credentials,
			RMessagesProcessor* answerProcessor, TMessageTransmitter* transmitter,
			ClientDetails* clientDetails) throw(StyxException);
	virtual bool isConnected();
	virtual IMessageTransmitter *getMessenger();
	virtual size_t getTimeout();
	virtual IVirtualStyxFile* getRoot();
	virtual StyxFID getRootFID();
	virtual ConnectionDetails getConnectionDetails();
	virtual ClientDetails *getRecepient();
	virtual void close() throw(StyxException);
	virtual StyxString getProtocol();
	virtual Credentials getCredentials();
	virtual StyxString getMountPoint();
	virtual StyxQID getQID();
	virtual void setAttached(bool isAttached);

    virtual void onSocketDisconnected(TMessageTransmitter *caller) ;
    virtual void onTrashReceived(TMessageTransmitter *caller);

};

#endif /* CONNECTION_H_ */
