#pragma once

#include "dataL4.h"

#ifdef USE_STD_MEMORY
    #include <memory>
#endif

namespace styxlib
{
    namespace serialization
    {
        class DeserializerL5;
        class SerializerL5;
    }

    namespace messages::base {
        class MessageFactory;
        class StyxMessage;
    }

    using DeserializerL5Ptr = serialization::DeserializerL5*;
    using SerializerL5Ptr = serialization::SerializerL5*;
    using MessageFactoryPtr = messages::base::MessageFactory*;
#ifdef USE_STD_MEMORY    
    using StyxMessageUPtr = std::unique_ptr<messages::base::StyxMessage>;
#else 
    using StyxMessageUPtr = messages::base::StyxMessage*;
#endif
}

#if __cplusplus >= 202302L
    #include "cxx_23/data.h"
#elif __cplusplus >= 201703L
    #include "cxx_17/data.h"
#else
    // Handle older standards (C++14, C++11, etc.)
    #error "This library requires at least C++17."
#endif