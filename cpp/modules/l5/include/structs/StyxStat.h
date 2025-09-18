#pragma once
#include "structs/StyxQID.h"
#include "data.h"

struct StyxStat
{
    int type; // for kernel use
    long dev; // for kernel use
    styxlib::structs::QID QID;
    long mode;                            // permissions and flags
    styxlib::StyxDate accessTime;         // last access time
    styxlib::StyxDate modificationTime;   // last modification time
    long length;                          // length of file in bytes
    styxlib::StyxString name;             // file name; must be / if the file is the root directory of the server
    styxlib::StyxString userName;         // owner name
    styxlib::StyxString groupName;        // group name
    styxlib::StyxString modificationUser; // name of the user who last modified the file

    const static StyxStat EMPTY;
};