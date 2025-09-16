#pragma once
#include "messages/v9p2000/BaseMessage.h"
#include "enums/MessageType.h"

namespace styxlib::messages::v9p2000
{
    class StyxTAttachMessage : public BaseMessage
    {
    public:
        const styxlib::Fid authFID;
        const StyxString userName;
        const StyxString mountPoint;

        StyxTAttachMessage(
            styxlib::Fid fid,
            styxlib::Fid authFID,
            const StyxString &userName,
            const StyxString &mountPoint)
            : BaseMessage(styxlib::enums::Tattach, 0, styxlib::structs::QID::EMPTY, fid, 0, ""),
              authFID(authFID),
              userName(userName),
              mountPoint(mountPoint)
        {
        }
    };
}