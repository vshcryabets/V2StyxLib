#pragma once
#include "messages/base/StyxMessage.h"
#include "ChannelTx.h"
#include "SerializerL4.h"

namespace styxlib::serialization
{
    class SerializerL5 : public SerializerL4
    {
    public:
        SerializerL5() : SerializerL4() {}
        virtual ~SerializerL5() = default;

        // Serialize and pass binary buffer to channelTx for transmission
        virtual ErrorCode sendMessage(
            const ClientId clientId,
            const StyxMessageUPtr &message
        ) = 0;
    };
}