#pragma once
#include <string>
#include <cstdint>
#include <memory>
#include <expected>

namespace styxlib
{
    enum class ErrorCode {
        Success = 0,
        AlreadyStarted,
        NotConnected,
        CantCreateSocket,
        CantBindSocket,
        CantListenSocket,
        CantCreateSocketPoll
    };

    using StyxString = std::string;
    using StyxDate = uint32_t;
    using StyxBuffer = uint8_t *;
    using Size = int16_t;
    using SizeResult = std::expected<Size, ErrorCode>;
    using Fid = uint32_t;
    using Tag = uint16_t;
    using Type = uint16_t;
    using ClientId = int;

    class SerializerL4;
    class DeserializerL4;
    class ChannelRx;
    class ChannelTx;
    class ChannelTxOneToMany;

    using SerializerL4Ptr = std::shared_ptr<SerializerL4>;
    using DeserializerL4Ptr = std::shared_ptr<DeserializerL4>;
    using ChannelRxPtr = std::shared_ptr<ChannelRx>;
    using ChannelTxPtr = std::shared_ptr<ChannelTx>;
    using ChannelTxOneToManyPtr = std::shared_ptr<ChannelTxOneToMany>;
}