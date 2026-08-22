#pragma once

#ifdef USE_STD_MEMORY
    #include <memory>
#endif

#include "serialization/DeserializerL5.h"

namespace styxlib
{
#ifdef USE_STD_MEMORY
    using DeserializerL5Ptr = std::shared_ptr<DeserializerL5>;
#else
    using DeserializerL5Ptr = DeserializerL5*;
#endif
}
