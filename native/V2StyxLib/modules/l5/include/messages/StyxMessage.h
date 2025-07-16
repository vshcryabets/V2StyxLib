#pragma once

#include <cstdint>
#include "data.h"

namespace styxlib::messages
{

    typedef uint8_t MessageType;
    typedef uint16_t MessageTag;

    class StyxMessage
    {
    public:
        const MessageType type;
        const MessageTag tag;

    public:
        StyxMessage(MessageType type, MessageTag tag);
    };

    class StyxRErrorMessage : public StyxMessage
    {
    public:
        const StyxString errorMessage;

    public:
        StyxRErrorMessage(MessageTag tag, const StyxString &message);
    };

}