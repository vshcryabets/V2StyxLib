package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public abstract class QueueMessagesProcessor implements IMessageProcessor {
    protected LinkedBlockingQueue<Pair> mQueue;
    protected Thread mThread;

    private class Pair {
        public StyxMessage mMessage;
        public int mClientId;
    }

    public QueueMessagesProcessor() {
        mQueue = new LinkedBlockingQueue<Pair>();
        mThread = new Thread(mRunnable, toString());
        mThread.start();
    }

    @Override
    public void postPacket(StyxMessage message, int clientId) {
        Pair pair = new Pair();
        pair.mMessage = message;
        pair.mClientId = clientId;
        mQueue.offer(pair);
    }

    @Override
    public void close() throws IOException {
        mThread.interrupt();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mThread.isInterrupted()) {
                try {
                    Pair pair = mQueue.poll(1, TimeUnit.SECONDS);
                    if ( pair != null ) {
                        try {
                            processPacket(pair.mMessage, pair.mClientId);
                        } catch (StyxException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };
}
