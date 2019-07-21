package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.messages.base.StyxTMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vshcryabets on 12/10/14.
 */
public class Polls {
    protected Map<Integer, StyxTMessage> mMessagesMap;
    protected MessageTagPoll mTags;
    protected FIDPoll mFids;

    public Polls() {
        mFids = new FIDPoll();
        mTags = new MessageTagPoll();
        mMessagesMap = new HashMap<Integer, StyxTMessage>();
    }

    public FIDPoll getFIDPoll() {
        return mFids;
    }

    public MessageTagPoll getTagPoll() {
        return mTags;
    }

    public void releaseTag(int tag) {
        mMessagesMap.remove(tag);
        mTags.release(tag);
    }

    public void releaseFID(StyxTMessageFID message) {
        mFids.release(message.getFID());
    }

    public void putTMessage(int tag, StyxTMessage message) {
        mMessagesMap.put(tag, message);
    }

    // TODO probably we can use one call instead getTMessage && releaseTag
    public StyxTMessage getTMessage(int tag) {
        return mMessagesMap.get(tag);
    }
}
