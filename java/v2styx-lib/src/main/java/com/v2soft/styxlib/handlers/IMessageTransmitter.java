package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.Closeable;

/**
 * Created by V.Shcryabets on 6/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageTransmitter extends Closeable {
    void sendMessage(StyxMessage answer, ClientDetails recipient) throws StyxException;
    int getTransmittedCount();
    int getErrorsCount();
    void clearStatistics();
}
