#pragma once
#include "structs/StyxQID.h"
#include "data.h"

struct StyxStat {
    int type; //for kernel use
    long dev; //for kernel use
    StyxQID QID;
    long mode; // permissions and flags
    StyxDate accessTime; // last access time
    StyxDate modificationTime; // last modification time
    long length; //length of file in bytes
    StyxString name; // file name; must be / if the file is the root directory of the server
    StyxString userName; //owner name
    StyxString groupName; //group name
    StyxString modificationUser; //name of the user who last modified the file

    const static StyxStat EMPTY;
};