#pragma once

#include "data.h"

namespace styxlib
{
    class ChannelTx
    {
    public:
        virtual ~ChannelTx() = default;
        virtual SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };
}

#if __cplusplus >= 202302L
    #include "cxx_23/ChannelTx.h"
#elif __cplusplus >= 201703L
    #include "cxx_17/ChannelTx.h"
#else
    // Handle older standards (C++14, C++11, etc.)
    #error "This library requires at least C++17."
#endif