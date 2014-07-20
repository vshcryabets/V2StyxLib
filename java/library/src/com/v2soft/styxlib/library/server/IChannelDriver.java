package com.v2soft.styxlib.library.server;

import com.v2soft.styxlib.library.core.IMessageProcessor;

import java.io.Closeable;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver extends Closeable, IMessageTransmitter {
    Thread start();
    void setMessageHandler(IMessageProcessor handler);
}
