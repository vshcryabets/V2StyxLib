#include "Client.h"

namespace styxlib
{
    Client::Client() = default;

    Client::~Client() = default;

    ClientBlocking::ClientBlocking()
        : channelTx(nullptr), channelRx(nullptr)
    {
    }

    ClientBlocking::~ClientBlocking() = default;

    void ClientBlocking::setChannels(ChannelTxPtr channelTx, ChannelRxPtr channelRx)
    {
        this->channelTx = channelTx;
        this->channelRx = channelRx;
    }

} // namespace styxlib