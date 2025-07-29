package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l6.StyxFile;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxTWalkMessage
extends StyxTMessageFID {
    private final long mNewFID;
    private final List<String> mPathElements;

    public StyxTWalkMessage(long fid, long new_fid, List<String> path){
        super(MessageType.Twalk, fid);
        mNewFID = new_fid;
        mPathElements = path;
    }

    public long getNewFID()
    {
        return mNewFID;
    }

    public int getPathLength() {
        if (mPathElements == null)
            return 0;
        return mPathElements.size();
    }

    public List<String> getPathElements() {
        return mPathElements;
    }

    @Override
    public String toString() {
        return String.format("%s\nNewFID: %d\nNumber of walks:%d\nPath: %s",
                super.toString(), mNewFID, mPathElements.size(), mPathElements.toString());
    }
}
