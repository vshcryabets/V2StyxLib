#pragma once

namespace styxlib
{
    typedef int ClientId;

    class ClientsRepo
    {
    public:
        virtual ClientId getNextClientId() = 0;
        virtual void releaseClientId(ClientId id) = 0;
    };
}
