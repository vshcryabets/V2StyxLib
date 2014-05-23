package com.v2soft.styxlib.library.server;

import com.v2soft.styxlib.library.core.IMessageProcessor;
import com.v2soft.styxlib.library.messages.base.StyxMessage;

import java.io.Closeable;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver extends Closeable {
    void sendMessage(ClientState client, StyxMessage answer);
    Thread start();
    void setMessageHandler(IMessageProcessor handler);
}
