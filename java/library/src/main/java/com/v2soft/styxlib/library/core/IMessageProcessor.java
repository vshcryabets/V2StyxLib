package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
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
    public void processPacket(StyxMessage message, ClientDetails transmitter) throws IOException;
    public int getReceivedPacketsCount();
    public int getReceivedErrorPacketsCount();
}
