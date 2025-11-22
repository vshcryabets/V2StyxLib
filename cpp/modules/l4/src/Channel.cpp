#include "Channel.h"

namespace styxlib
{
    ChannelRx::ChannelRx() : deserializer(nullptr)
    {
    }

    ErrorCode ChannelRx::setDeserializer(DeserializerL4Ptr deserializer)
    {
        if (deserializer == nullptr)
        {
            return ErrorCode::NullptrArgument;
        }
        this->deserializer = deserializer;
        return ErrorCode::Success;
    }

} // namespace styxlib