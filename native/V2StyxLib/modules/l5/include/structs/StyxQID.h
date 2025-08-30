#pragma once

#include <cstdint>

namespace styxlib::structs
{
    struct QID
    {
        uint8_t type;
        uint32_t version;
        uint64_t path;

        constexpr QID(uint8_t t, uint32_t v, uint64_t p)
            : type(t), version(v), path(p) {}

        const static QID EMPTY;
    };
}