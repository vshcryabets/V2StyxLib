package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageProcessor extends Closeable {
    public void addClient(ClientDetails state);
    public void removeClient(ClientDetails state);
    public void postPacket(StyxMessage message, ClientDetails target);
    public void processPacket(StyxMessage message, ClientDetails target) throws IOException;
    public int getReceivedPacketsCount();
    public int getReceivedErrorPacketsCount();
}
