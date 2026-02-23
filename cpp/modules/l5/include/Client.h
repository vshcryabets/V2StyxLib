#pragma once

#include "messages/base/StyxMessage.h"
#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{

class Client {
public:
    Client();
    virtual ~Client();
    virtual bool isConnected() const = 0;
};

class ClientBlocking: public Client {
protected:
    ChannelTxPtr channelTx;
    ChannelRxPtr channelRx;
public:
    ClientBlocking();
    ~ClientBlocking() override;
    /**
     * Sets the transmission and reception channels for the client.
     * @param channelTx The transmission channel.
     * @param channelRx The reception channel.
     */
    void setChannels(ChannelTxPtr channelTx, ChannelRxPtr channelRx);
    /**
     * Sends a Styx message in a blocking manner and wait for its completion.
     * @param message The Styx message to send.
     * @return The response Styx message.
     */
    virtual messages::base::StyxMessage sendBlocking(
        const messages::base::StyxMessage &message
    ) = 0;
};

} // namespace styxlib