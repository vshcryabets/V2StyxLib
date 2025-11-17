#pragma once

#include "ClientsRepo.h"
#include <atomic>

namespace styxlib
{
    class ClientsRepoImpl : public styxlib::ClientsRepo
    {
    private:
        std::atomic_int nextId{InvalidClientId + 1};

    public:
        styxlib::ClientId getNextClientId() override;
        void releaseClientId(styxlib::ClientId id) override;
        styxlib::ClientId getLastIssuedId() const;
    };
} // namespace styxlib