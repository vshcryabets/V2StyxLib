#pragma once

#include "dataL5.h"
#include "serialization/DeserializerL5.h"
#include "structs/StyxQID.h"
#include "structs/StyxStat.h"
#include "messages/v9p2000/MessageFactoryImpl.h"
#include "serialization/IBuffer.h"

namespace styxlib::serialization
{
    class DeserializerL5StyxImpl : public DeserializerL5
    {
    private:
        messages::v9p2000::MessageFactoryImpl messageFactory;

        structs::QID deserializeQid(serialization::IBufferReader &input) const;
        structs::StyxStat deserializeStat(serialization::IBufferReader &input) const;
        StyxMessageExpected deserializeMessage(
            serialization::IBufferReader &input) const;

    public:
        explicit DeserializerL5StyxImpl() = default;

        ~DeserializerL5StyxImpl() override = default;

        ErrorCode handleBuffer(
            ClientId clientId,
            const StyxBuffer buffer,
            Size size) override;
    };
}
