package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public abstract class QueueMessagesProcessor implements IMessageProcessor {
    protected ThreadPoolExecutor mExecutor;

    public QueueMessagesProcessor() {
        mExecutor = new ThreadPoolExecutor(1, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void postPacket(final StyxMessage message, final ClientDetails target) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    processPacket(message, target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
