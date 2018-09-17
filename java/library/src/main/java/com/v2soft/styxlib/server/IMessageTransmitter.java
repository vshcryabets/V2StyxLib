package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.utils.SyncObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by V.Shcryabets on 6/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IMessageTransmitter extends Closeable {
    boolean sendMessage(StyxMessage answer, ClientDetails recepient) throws IOException;
    StyxMessage sendMessageAndWaitAnswer(StyxMessage answer, ClientDetails recepient, SyncObject syncObject)
            throws IOException, InterruptedException, StyxErrorMessageException, TimeoutException;
    int getTransmittedCount();
    int getErrorsCount();
}
