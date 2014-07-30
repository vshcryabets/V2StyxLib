package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientState;

import java.io.IOException;

/**
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class MessagesFilter implements IMessageProcessor {
    protected IMessageProcessor mTProcessor;
    protected IMessageProcessor mRProcessor;

    public MessagesFilter(IMessageProcessor tProcessor, IMessageProcessor rProcessor) {
        mTProcessor = tProcessor;
        mRProcessor = rProcessor;
    }

    @Override
    public void addClient(ClientState state) {
        if ( mTProcessor != null ) {
            mTProcessor.addClient(state);
        }
        if ( mRProcessor != null ) {
            mRProcessor.addClient(state);
        }
    }

    @Override
    public void removeClient(ClientState state) {
        if ( mTProcessor != null ) {
            mTProcessor.addClient(state);
        }
        if ( mRProcessor != null ) {
            mRProcessor.addClient(state);
        }
    }

    @Override
    public void processPacket(StyxMessage message) throws IOException {
        if ( message.getType().isTMessage() ) {
            if ( mTProcessor != null ) {
                mTProcessor.processPacket(message);
            }
        } else {
            if ( mRProcessor != null ) {
                mRProcessor.processPacket(message);
            }
        }
    }

    @Override
    public int getReceivedPacketsCount() {
        return 0;
    }

    @Override
    public int getReceivedErrorPacketsCount() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        if ( mTProcessor != null ) {
            mTProcessor.close();
        }
        if ( mRProcessor != null ) {
            mRProcessor.close();
        }
    }

    public void setTProcessor(IMessageProcessor tMessageProcessor) {
        mTProcessor = tMessageProcessor;
    }

    public IMessageProcessor getTProcessor() {
        return mTProcessor;
    }
}
