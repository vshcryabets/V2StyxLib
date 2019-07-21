package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public abstract class QueueMessagesProcessor implements IMessageProcessor, Runnable {
    protected LinkedBlockingQueue<Pair> mQueue;
    protected Thread mThread;
    protected int mHandledPackets;
    protected int mErrorPackets;
    protected String mTag;

    private class Pair {
        public StyxMessage mMessage;
        public ClientDetails mTransmitter;
    }

    public QueueMessagesProcessor(String tag) {
        mTag = tag;
        mHandledPackets = 0;
        mErrorPackets = 0;
        mQueue = new LinkedBlockingQueue<>();
        mThread = new Thread(this, tag);
        mThread.start();
    }

    @Override
    public void addClient(ClientDetails state) {
        // nothing to do
    }

    @Override
    public void removeClient(ClientDetails state) {
        // nothing to do
    }

    @Override
    public void postPacket(StyxMessage message, ClientDetails target) {
        Pair pair = new Pair();
        pair.mMessage = message;
        pair.mTransmitter = target;
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

    @Override
    public void run() {
        while (!mThread.isInterrupted()) {
            try {
                Pair pair = mQueue.poll(1, TimeUnit.SECONDS);
                if ( pair != null ) {
                    try {
                        processPacket(pair.mMessage, pair.mTransmitter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public int getReceivedPacketsCount() {
        return mHandledPackets;
    }

    @Override
    public int getReceivedErrorPacketsCount() {
        return mErrorPackets;
    }

}
