package com.v2soft.styxlib.server;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.handlers.IMessageProcessor;

import java.io.Closeable;
import java.util.Collection;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver extends IMessageTransmitter {
    /**
     * Start channel driver thread.
     * @param iounit
     * @return driver thread.
     */
    Thread start(int iounit);
    void setTMessageHandler(IMessageProcessor handler);
    void setRMessageHandler(IMessageProcessor handler);
    void setLogListener(ILogListener listener);

    /**
     * Get all active clients.
     * @return all active clients.
     */
    Collection<ClientDetails> getClients();

    /**
     * Is connected to another peer?
     * @return true if connected to another peer.
     */
    boolean isConnected();

    /**
     * Is driver started?
     * @return true if driver working.
     */
    boolean isStarted();

    IMessageProcessor getTMessageHandler();
    IMessageProcessor getRMessageHandler();
}
