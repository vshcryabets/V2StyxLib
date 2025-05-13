package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;

public class EmptyMessagesProcessor implements IMessageProcessor {
    @Override
    public void onClientRemoved(int clientId) { }

    @Override
    public void onClientMessage(StyxMessage message, int clientId) {}

    @Override
    public void close() { }
}
