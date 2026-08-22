#pragma once

#include "dataL5.h"
#include "serialization/DeserializerL5.h"
#include "structs/StyxQID.h"
#include "structs/StyxStat.h"
#include "messages/v9p2000/MessageFactoryImpl.h"
#include "serialization/IBuffer.h"

namespace styxlib
{
    class DeserializerL5StyxImpl : public DeserializerL5
    {
    private:
        messages::v9p2000::MessageFactoryImpl messageFactory;
        Size ioUnit;

        structs::QID deserializeQid(serialization::IBufferReader &input) const;
        StyxStat deserializeStat(serialization::IBufferReader &input) const;
        StyxMessageExpected deserializeMessage(
            serialization::IBufferReader &input,
            Size packetLimit) const;

    public:
        explicit DeserializerL5StyxImpl(Size ioUnit = 8192)
            : DeserializerL5(), ioUnit(ioUnit)
        {
        }

        ~DeserializerL5StyxImpl() override = default;

        ErrorCode handleBuffer(
            ClientId clientId,
            const StyxBuffer buffer,
            Size size) override;
    };
}
