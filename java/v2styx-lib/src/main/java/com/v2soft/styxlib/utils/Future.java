package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxRErrorMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class Future<T extends StyxMessage>  {
    private CompletableFuture<T> inner;

    public Future(CompletableFuture<T> inner) {
        this.inner = inner;
    }

    public T getResult(long timeoutMs) throws StyxException {
        try {
            var answer = inner
                    .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .join();
            if (answer.getType() == MessageType.Rerror) {
                StyxRErrorMessage errorMessage = (StyxRErrorMessage) answer;
                throw StyxErrorMessageException.newInstance(errorMessage.mError);
            }
            return answer;
        } catch (CompletionException err) {
            final var cause = err.getCause();
            if (cause instanceof StyxException) {
                throw (StyxException)cause;
            } else {
                throw err;
            }
        }
    }
}
