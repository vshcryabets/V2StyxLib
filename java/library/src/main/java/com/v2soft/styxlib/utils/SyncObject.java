package com.v2soft.styxlib.utils;

public class SyncObject {
    private long mTimeout;

    /**
     *
     */
    public SyncObject(long timeout) {
       mTimeout = timeout;
    }

    public void waitForNotify()
            throws InterruptedException {
        wait(mTimeout);
    }

    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long timeout) {
        this.mTimeout = timeout;
    }
}
