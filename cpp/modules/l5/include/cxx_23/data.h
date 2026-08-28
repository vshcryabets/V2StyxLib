#pragma once

#include <expected>

namespace styxlib
{
    using StyxMessageExpected = std::expected<StyxMessageUPtr, ErrorCode>;
}


