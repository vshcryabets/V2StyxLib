#include "messages/v9p2000/BaseMessage.h"

namespace styxlib::messages::v9p2000
{

    BaseMessage::BaseMessage(
        Type type,
        Tag tag,
        const styxlib::structs::QID &qid,
        styxlib::Fid fid,
        uint32_t iounit,
        const StyxString &protocolVersion)
        : StyxMessage(type, tag),
          qid(qid),
          fid(fid),
          iounit(iounit),
          protocolVersion(protocolVersion)
    {
    }

}