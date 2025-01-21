package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.exceptions.StyxException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Future<T>  {
    private CompletableFuture<T> inner;

    public Future(CompletableFuture<T> inner) {
        this.inner = inner;
    }

    public T getResult() throws StyxException {
        try {
            return inner.join();
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
