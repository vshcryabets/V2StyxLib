package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;

import java.util.concurrent.CompletableFuture;

public interface CompletablesMap {
    void assignAnswer(int tag, StyxMessage answer);
    void addCompletable(int tag, CompletableFuture<StyxMessage> completable);
}
