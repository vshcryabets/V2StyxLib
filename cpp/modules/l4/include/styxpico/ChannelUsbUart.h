#pragma once

#include "ChannelTx.h"

namespace styxlib
{
    class ChannelUsbUart : public ChannelTx
    {
    private:
        PacketHeaderSize packetSizeHeader;
    public:
        ChannelUsbUart(const PacketHeaderSize &packetSizeHeader);
        virtual ~ChannelUsbUart();
        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
    };
}