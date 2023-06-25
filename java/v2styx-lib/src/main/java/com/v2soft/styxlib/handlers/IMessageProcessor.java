package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageProcessor extends Closeable {
    void addClient(ClientDetails state);
    void removeClient(ClientDetails state);
    void postPacket(StyxMessage message, ClientDetails target);
    void processPacket(StyxMessage message, ClientDetails target) throws IOException;
    int getReceivedPacketsCount();
    int getReceivedErrorPacketsCount();
}
