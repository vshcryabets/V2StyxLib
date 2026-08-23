#pragma once

namespace styxlib
{
    /**
     * Sender class responsible for message preparation and transmission over the channel. 
     * For example it should find appropriate message tag.
     */
    class SenderL5
    {
    public:
        SenderL5() = default;
        virtual ~SenderL5() = default;

        virtual void sendMessage(
            const ClientId clientId,
            const StyxMessageUPtr &message
        ) = 0;
    };
}