package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.Checks;
import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.server.ClientsRepo;
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
    protected ClientsRepo mClientsRepo;

    public TMessageTransmitter(Listener listener, ClientsRepo clientsRepo) {
        mListener = listener;
        mClientsRepo = clientsRepo;
    }

    @Override
    public <R extends StyxMessage> Future<R> sendMessage(
            StyxMessage message,
            int clientId,
            long timeout
            )
            throws StyxException {
        if ( !Checks.isTMessage(message.type)) {
            throw new StyxException("Can't sent RMessage");
        }
        if (clientId < 0) {
            throw new StyxException("clientId is negative");
        }
        final var driver = mClientsRepo.getChannelDriver(clientId);
        final var polls = mClientsRepo.getPolls(clientId);
        if (!driver.isConnected()) throw new StyxException("Not connected to server");

        // set message tag
        int tag = Constants.NOTAG;
        if (message.type != MessageType.Tversion) {
            tag = mClientsRepo.getPolls(clientId).getTagPoll().getFreeItem();
        }
        message.setTag((short) tag);
        polls.getMessagesMap().put(tag, (StyxTMessage) message);
        mTransmittedCount++;
        return driver.sendMessage(message, clientId, timeout);
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
