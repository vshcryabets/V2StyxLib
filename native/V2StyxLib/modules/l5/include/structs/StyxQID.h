#pragma once

#include <cstdint>

struct StyxQID {
public:
    uint8_t type;
    uint32_t version;
    uint64_t path;
};