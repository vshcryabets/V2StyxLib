#pragma once
#include <string>
#include <cstdint>
#include <memory>

namespace styxlib
{
    enum class ErrorCode: uint8_t {
        Success = 0,
        AlreadyStarted,
        NotConnected,
        CantCreateSocket,
        CantBindSocket,
        CantListenSocket,
        CantCreateSocketPoll,
        PacketTooLarge,
        UnknownClient,
        BufferTooSmall,
        InvalidHeaderSize
    };

    enum class PacketHeaderSize : uint8_t
    {
        Size1Byte = 1,
        Size2Bytes = 2,
        Size4Bytes = 4
    };

    using StyxString = std::string;
    using StyxDate = uint32_t;
    using StyxBuffer = uint8_t *;
    using Size = uint16_t;
    using Fid = uint32_t;
    using Tag = uint16_t;
    using Type = uint16_t;
    using ClientId = uint16_t;

    constexpr ClientId InvalidClientId = 0;

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

    inline uint8_t to_uint8_t(const PacketHeaderSize &headerSize)
    {
        return static_cast<uint8_t>(headerSize);
    }
}

#if __cplusplus >= 202302L
    #include "cxx_23/data.h"
#elif __cplusplus >= 201703L
    #include "cxx_17/data.h"
#else
    // Handle older standards (C++14, C++11, etc.)
    #error "This library requires at least C++17."
#endif