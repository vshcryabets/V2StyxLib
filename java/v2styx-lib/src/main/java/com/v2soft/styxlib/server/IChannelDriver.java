package com.v2soft.styxlib.server;

import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;

import java.io.Closeable;
import java.util.Collection;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver extends Closeable, IMessageTransmitter {
    Thread start(int iounit);
    void setTMessageHandler(IMessageProcessor handler);
    void setRMessageHandler(IMessageProcessor handler);

    /**
     * Get all active clients.
     * @return all active clients.
     */
    Collection<Integer> getClients();

    boolean isConnected();
    boolean isStarted();
    IDataSerializer getSerializer();
    IDataDeserializer getDeserializer();
}
