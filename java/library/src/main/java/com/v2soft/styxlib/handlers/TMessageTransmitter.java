package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.utils.Polls;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by vshcryabets on 12/8/14.
 */
public class TMessageTransmitter implements IMessageTransmitter {
    public interface Listener {
        void onSocketDisconnected(); // TODO why socket?
        void onTrashReceived();
    }

    protected int mTransmittedCount, mErrorCount;
    protected Polls mPolls;
    protected Listener mListener;

    public TMessageTransmitter(Polls polls, Listener listener) {
        if ( polls == null ) {
            throw new NullPointerException("polls is null");
        }
        mPolls = polls;
        mListener = listener;
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
                tag = mPolls.getTagPoll().getFreeItem();
            }
            message.setTag((short) tag);
            mPolls.getMessagesMap().put(tag, (StyxTMessage) message);

            driver.sendMessage(message, recepient);
            mTransmittedCount++;
            return true;
        } catch (SocketException e) {
            mListener.onSocketDisconnected();
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
