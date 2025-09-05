#pragma once
#include "messages/v9p2000/BaseMessage.h"
#include "enums/MessageType.h"

namespace styxlib::messages::v9p2000
{
    class StyxTAuthMessage : public BaseMessage
    {
    public:
        const StyxString userName;
        const StyxString mountPoint;

        StyxTAuthMessage(
            styxlib::Fid fid,
            const StyxString &userName,
            const StyxString &mountPoint)
            : BaseMessage(styxlib::enums::Tauth, 0, styxlib::structs::QID::EMPTY, fid, 0, ""),
              userName(userName),
              mountPoint(mountPoint)
        {
        }
    };
}