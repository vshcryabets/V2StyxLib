#include "structs/StyxStat.h"

const StyxStat StyxStat::EMPTY = {
        .type = 0,
        .dev = 0,
        .QID = StyxQID::EMPTY,
        .mode = 0,
        .accessTime = 0,
        .modificationTime = 0,
        .length = 0,
        .name = "",
        .userName = "",
        .groupName = "",
        .modificationUser = ""
    };