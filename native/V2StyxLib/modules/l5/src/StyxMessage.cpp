#include "messages/base/StyxMessage.h"
#include "enums/MessageType.h"

namespace styxlib::messages::base
{

    StyxMessage::StyxMessage(Type type, Tag tag)
        : type(type), tag(tag)
    {
    }
}
