package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;

import java.io.Closeable;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageProcessor extends Closeable {
    void onClientRemoved(int clientId);
    void onClientMessage(StyxMessage message, int clientId);
}
