package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.utils.Future;

/**
 * Created by V.Shcryabets on 6/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageTransmitter {
    int getTransmittedCount();
    int getErrorsCount();
    void clearStatistics();
    void close();
    <R extends StyxMessage> Future<R> sendMessage(StyxMessage message,
                                                  int clientId) throws StyxException;
}
