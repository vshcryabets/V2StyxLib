package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessage;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.utils.SyncObject;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vshcryabets on 12/8/14.
 */
// TODO why do we need this class? We can directly use ChannelDriver.
public class TMessageTransmitter implements IMessageTransmitter {
    public interface Listener {
	    // TODO pass caller
        void onSocketDisconnected(TMessageTransmitter caller); // TODO why socket?
        void onTrashReceived(TMessageTransmitter caller);
    }

    protected int mTransmittedCount, mErrorCount;
    protected Listener mListener;

    public TMessageTransmitter(Listener listener) {
        mListener = listener;
    }

    @Override
    public StyxMessage sendMessageAndWaitAnswer(StyxMessage message, ClientDetails recepient, SyncObject syncObject)
            throws IOException, InterruptedException, StyxErrorMessageException, TimeoutException {
        if (!message.getType().isTMessage()) {
            throw new IllegalArgumentException("Can't wait answer for RMassage");
        }
        StyxTMessage tMessage = (StyxTMessage) message;
        tMessage.setSyncObject(syncObject);
        sendMessage(tMessage, recepient);
        return tMessage.waitForAnswer();
    }

    @Override
    public boolean sendMessage(StyxMessage message, ClientDetails recepient) throws IOException {
        if ( !message.getType().isTMessage() ) {
            throw new IOException("Can't sent RMessage");
        }
        if (recepient == null) {
            throw new NullPointerException("Recepient is null");
        }
        try {
            IChannelDriver driver = recepient.getDriver();
            if (!driver.isConnected()) throw new IOException("Not connected to server");

            // set message tag
            int tag = StyxMessage.NOTAG;
            if (message.getType() != MessageType.Tversion) {
                tag = recepient.getPolls().getTagPoll().getFreeItem();
            }
            message.setTag((short) tag);
            recepient.getPolls().putTMessage(tag, (StyxTMessage) message);

            driver.sendMessage(message, recepient);
            mTransmittedCount++;
            return true;
        } catch (SocketException e) {
            if ( mListener != null ) {
                mListener.onSocketDisconnected(this);
            }
        }
        return false;
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
    public void close() throws IOException {

    }
}
