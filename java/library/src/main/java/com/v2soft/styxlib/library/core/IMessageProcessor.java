package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientState;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageProcessor extends Closeable {
    public void addClient(ClientState state);
    public void removeClient(ClientState state);
    public void processPacket(StyxMessage message, ClientState transmitter) throws IOException;
    public int getReceivedPacketsCount();
    public int getReceivedErrorPacketsCount();
}
