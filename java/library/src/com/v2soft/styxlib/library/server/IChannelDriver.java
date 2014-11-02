package com.v2soft.styxlib.library.server;

import com.v2soft.styxlib.library.core.IMessageProcessor;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver extends Closeable, IMessageTransmitter {
    Thread start();
    void setMessageHandler(IMessageProcessor handler);

    /**
     * Get all active clients.
     * @return all active clients.
     */
    Set<ClientState> getClients();

    boolean isConnected();
}
