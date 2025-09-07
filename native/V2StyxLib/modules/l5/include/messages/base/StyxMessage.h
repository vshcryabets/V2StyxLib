#pragma once

#include "data.h"
#include <memory>

namespace styxlib::messages::base
{

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

    typedef std::unique_ptr<StyxMessage> StyxMessageUPtr;
}