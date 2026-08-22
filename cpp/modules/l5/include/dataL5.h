#pragma once

#ifdef USE_STD_MEMORY
    #include <memory>
#endif

#include "serialization/DeserializerL5.h"
#include "serialization/SerializerL5.h"

namespace styxlib
{
#ifdef USE_STD_MEMORY
    using DeserializerL5Ptr = std::shared_ptr<DeserializerL5>;
    using SerializerL5Ptr = std::shared_ptr<SerializerL5>;
#else
    using DeserializerL5Ptr = DeserializerL5*;
    using SerializerL5Ptr = SerializerL5*;
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