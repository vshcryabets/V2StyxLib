#pragma once

#include "dataL4.h"
#include <memory>

namespace styxlib::messages::base
{
    // TODO rename to Message
    class StyxMessage
    {
    private:
        Type type;
        Tag tag;
    public:
        StyxMessage(Type type, Tag tag);
        virtual ~StyxMessage() = default;
        Type getType() const { return type; }
        Tag getTag() const { return tag; }
    };

}