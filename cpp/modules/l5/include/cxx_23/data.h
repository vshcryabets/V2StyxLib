#pragma once

#include <expected>

namespace styxlib
{
    using StyxMessageExpected = std::expected<messages::base::StyxMessageUPtr, ErrorCode>;
}


