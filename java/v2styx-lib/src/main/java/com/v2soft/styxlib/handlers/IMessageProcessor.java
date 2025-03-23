package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
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
    void addClient(int clientId);
    void removeClient(int clientId);
    void postPacket(StyxMessage message,int clientId);
    void processPacket(StyxMessage message, int clientId) throws StyxException;
    int getReceivedPacketsCount();
    int getReceivedErrorPacketsCount();
}
