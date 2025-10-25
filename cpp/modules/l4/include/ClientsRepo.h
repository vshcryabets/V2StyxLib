#pragma once
#include "data.h"

namespace styxlib
{
    class ClientsRepo
    {
    public:
        virtual ClientId getNextClientId() = 0;
        virtual void releaseClientId(ClientId id) = 0;
    };
}
