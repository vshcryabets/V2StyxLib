#pragma once

#include <cstdint>

namespace styxlib::messages {

typedef uint8_t MessageType;
typedef uint16_t MessageTag;

class StyxMessage
{
public:
    const MessageType type;
    const MessageTag tag;
public:
    StyxMessage(MessageType type, MessageTag tag);
    ~StyxMessage();
};

}