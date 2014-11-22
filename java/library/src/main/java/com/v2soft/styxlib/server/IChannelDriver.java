package com.v2soft.styxlib.server;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.core.IMessageProcessor;

import java.io.Closeable;
import java.util.Set;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver extends Closeable, IMessageTransmitter {
    Thread start(int iounit);
    void setMessageHandler(IMessageProcessor handler);
    void setLogListener(ILogListener listener);

    /**
     * Get all active clients.
     * @return all active clients.
     */
    Set<ClientDetails> getClients();

    boolean isConnected();
    boolean isStarted();
}
