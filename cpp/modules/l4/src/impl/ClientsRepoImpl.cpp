#include "impl/ClientsRepoImpl.h"

namespace styxlib
{
    styxlib::ClientId ClientsRepoImpl::getNextClientId()
    {
        return ++nextId;
    }

    void ClientsRepoImpl::releaseClientId(styxlib::ClientId id)
    {
        // No-op for simple implementation
    }

    styxlib::ClientId ClientsRepoImpl::getLastIssuedId() const
    {
        return nextId.load();
    }
} // namespace styxlib
