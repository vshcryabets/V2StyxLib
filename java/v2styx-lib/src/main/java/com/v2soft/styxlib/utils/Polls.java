package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.Logger;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vshcryabets on 12/10/14.
 */
public class Polls implements CompletablesMap {
    protected MessageTagPoll mTags;
    protected FIDPoll mFids;
    protected Map<Integer, CompletableFuture<StyxMessage>> mMessagesMap = new ConcurrentHashMap<>();

    public Polls() {
        mFids = new FIDPoll();
        mTags = new MessageTagPoll();
    }

    public FIDPoll getFIDPoll() {
        return mFids;
    }

    public MessageTagPoll getTagPoll() {
        return mTags;
    }

    @Override
    public void assignAnswer(int tag, StyxMessage answer) {
        var completable = mMessagesMap.get(tag);
        if (completable != null) {
            completable.complete(answer);
        }
    }

    @Override
    public void addCompletable(int tag, CompletableFuture<StyxMessage> completable) {
        mMessagesMap.put(tag, completable);
    }

    public void releaseTag(int tag) {
        mMessagesMap.remove(tag);
        mTags.release(tag);
    }

    public void releaseFID(StyxTMessageFID message) {
        if (Config.DEBUG_FID_POLL)
            Logger.DEBUG.println("releaseFID " + message.getFID());
        mFids.release(message.getFID());
    }
}
