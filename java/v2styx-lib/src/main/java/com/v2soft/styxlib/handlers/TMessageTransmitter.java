package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.utils.Future;

/**
 * Created by vshcryabets on 12/8/14.
 */
public class TMessageTransmitter implements IMessageTransmitter {
    public interface Listener {
        void onLostConnection();
        void onTrashReceived();
    }

    protected int mTransmittedCount, mErrorCount;
    protected Listener mListener;

    public TMessageTransmitter(Listener listener) {
        mListener = listener;
    }

    @Override
    public <R extends StyxMessage> Future<R> sendMessage(
            StyxMessage message,
            ClientDetails recipient,
            long timeout
            )
            throws StyxException {
        if ( !message.getType().isTMessage() ) {
            throw new StyxException("Can't sent RMessage");
        }
        if (recipient == null) {
            throw new StyxException("Recipient is null");
        }

        IChannelDriver driver = recipient.getDriver();
        if (!driver.isConnected()) throw new StyxException("Not connected to server");

        // set message tag
        int tag = StyxMessage.NOTAG;
        if (message.getType() != MessageType.Tversion) {
            tag = recipient.getPolls().getTagPoll().getFreeItem();
        }
        message.setTag((short) tag);
        recipient.getPolls().getMessagesMap().put(tag, (StyxTMessage) message);
        mTransmittedCount++;
        return driver.sendMessage(message, recipient, timeout);
    }

    @Override
    public int getTransmittedCount() {
        return mTransmittedCount;
    }

    @Override
    public int getErrorsCount() {
        return mErrorCount;
    }

    @Override
    public void clearStatistics() {
        mTransmittedCount = 0;
        mErrorCount = 0;
    }

    @Override
    public void close() {

    }
}
