#pragma once
#include "messages/base/StyxMessage.h"
#include "structs/StyxQID.h"
#include "enums/MessageType.h"

namespace styxlib::messages::v9p2000
{

    class BaseMessage : public styxlib::messages::base::StyxMessage
    {
    protected:
        styxlib::structs::QID qid;
        styxlib::Fid fid;
        uint32_t iounit;
        StyxString protocolVersion;

    public:
        BaseMessage(
            Type type,
            Tag tag,
            const styxlib::structs::QID &qid,
            styxlib::Fid fid,
            uint32_t iounit,
            const StyxString &protocolVersion);
        virtual ~BaseMessage() = default;
        styxlib::structs::QID getQID() const { return qid; }
        styxlib::Fid getFID() const { return fid; }
        uint32_t getIounit() const { return iounit; }
        const StyxString &getProtocolVersion() const { return protocolVersion; }
    };

    class StyxRErrorMessage : public BaseMessage
    {
    public:
        StyxRErrorMessage(
            Tag tag,
            const StyxString &errorMessage)
            : BaseMessage(styxlib::enums::Rerror, tag, styxlib::structs::QID::EMPTY, 0, 0, ""),
              errorMessage(errorMessage)
        {
        }

        const StyxString &getMessage() const { return errorMessage; }

    private:
        StyxString errorMessage;
    };

}