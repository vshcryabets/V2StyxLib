#include "messages/StyxMessage.h"
#include "enums/MessageType.h"

namespace styxlib::messages
{

    StyxMessage::StyxMessage(MessageType type, MessageTag tag)
        : type(type), tag(tag)
    {
    }

    StyxRErrorMessage::StyxRErrorMessage(MessageTag tag, const StyxString &message)
        : StyxMessage(styxlib::enums::Rerror, 0), errorMessage(message)
    {
        }

}
