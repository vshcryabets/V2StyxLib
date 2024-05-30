package com.v2soft.styxlib.l5.structs;

import java.util.Date;

record StyxStat2 (
        int mType, //for kernel use
        long mDev, //for kernel use
        StyxQID mQID,
        long mMode, // permissions and flags
        Date mAccessTime, // last access time
        Date mModificationTime, // last modification time
        long mLength, //length of file in bytes
        String mName, // file name; must be / if the file is the root directory of the server
        String mUserName, //owner name
        String mGroupName, //group name
        String mModificationUser //name of the user who last modified the file
) {

}
