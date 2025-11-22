#pragma once
#include <expected>

namespace styxlib
{
    using SizeResult = std::expected<Size, ErrorCode>;
}