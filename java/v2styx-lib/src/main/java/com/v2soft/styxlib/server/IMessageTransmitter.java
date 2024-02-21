package com.v2soft.styxlib.server;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by V.Shcryabets on 6/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageTransmitter extends Closeable {
    boolean sendMessage(StyxMessage answer, ClientDetails recipient) throws IOException;
    int getTransmittedCount();
    int getErrorsCount();
}
