package com.v2soft.styxlib.l5.structs;

import java.util.Date;

public record StyxStat(
        int type, //for kernel use
        long dev, //for kernel use
        QID QID,
        long mode, // permissions and flags
        Date accessTime, // last access time
        Date modificationTime, // last modification time
        long length, //length of file in bytes
        String name, // file name; must be / if the file is the root directory of the server
        String userName, //owner name
        String groupName, //group name
        String modificationUser //name of the user who last modified the file
) {
    public static StyxStat EMPTY = new StyxStat(
            (short) 0,
            0,
            com.v2soft.styxlib.l5.structs.QID.EMPTY,
            0,
            new Date(),
            new Date(),
            0,
            "",
            "",
            "",
            "");

}
