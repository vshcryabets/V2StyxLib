#pragma once

#include "structs/StyxStat.h"
#include "serialization/IDataSerializer.h"
#include "serialization/SerializerL5.h"

namespace styxlib::serialization
{

    class SerializerL5StyxImpl : public IDataSerializer, public SerializerL5
    {
    public:
        SerializerL5StyxImpl(): IDataSerializer(), SerializerL5() {};
        virtual ~SerializerL5StyxImpl() {};

        styxlib::Size 
        getMessageSize(const styxlib::messages::base::StyxMessage &message) const override;

        void serialize(
            const styxlib::messages::base::StyxMessage &message,
            styxlib::serialization::IBufferWriter &output
        ) override;

        void serializeStat(
            const styxlib::structs::StyxStat &stat, 
            styxlib::serialization::IBufferWriter &output) override;

        styxlib::Size 
        getStatSerializedSize(const styxlib::structs::StyxStat &stat) override;

        styxlib::Size getQidSize() override;
        
        void 
        serializeQid(
            const styxlib::structs::QID &qid, 
            styxlib::serialization::IBufferWriter &output
        ) override;

        ErrorCode
        sendMessage(
            const styxlib::ClientId clientId,
            const styxlib::StyxMessageUPtr &message
        ) override;
    };

} // namespace styxlib
