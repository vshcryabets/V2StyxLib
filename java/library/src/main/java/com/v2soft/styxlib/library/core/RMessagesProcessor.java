package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.library.utils.MessageTagPoll;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class RMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {
    public interface Listener {
        void onFIDReleased(long fid);
    }

    protected int mReceivedCount, mErrorCount;
    protected Map<Integer, StyxTMessage> mMessagesMap;
    protected Listener mListener;
    protected MessageTagPoll mActiveTags;


    private class Pair {
        public StyxMessage mMessage;
        public ClientDetails mTransmitter;
    }

    public RMessagesProcessor() {
        super();
        mActiveTags = new MessageTagPoll();
        mMessagesMap = new HashMap<Integer, StyxTMessage>();
    }

    @Override
    public void addClient(ClientDetails state) {

    }

    @Override
    public void removeClient(ClientDetails state) {

    }

    public MessageTagPoll getActiveFIDs() {
        return mActiveTags;
    }


    public Map<Integer, StyxTMessage> getMessagesMap() {
        return mMessagesMap;
    }

    @Override
    public void processPacket(StyxMessage message, ClientDetails transmitter) throws IOException {
        mReceivedCount++;
        int tag = message.getTag();
        if (!mMessagesMap.containsKey(tag)) {
            // we didn't send T message with such tag, so ignore this R message
            System.err.println("Got unknown R message from client "+transmitter);
            return;
        }
        final StyxTMessage tMessage = mMessagesMap.get(tag);
        if (tMessage.getType() == MessageType.Tclunk ||
                tMessage.getType() == MessageType.Tremove) {
            mListener.onFIDReleased(((StyxTMessageFID) tMessage).getFID());
        }
        try {
            tMessage.setAnswer(message);
        } catch (StyxException e) {
            e.printStackTrace();
        }
        if (message.getType() == MessageType.Rerror) {
            mErrorCount++;
        }
        mMessagesMap.remove(tag);
        mActiveTags.release(tag);
    }

    @Override
    public int getReceivedPacketsCount() {
        return mReceivedCount;
    }

    @Override
    public int getReceivedErrorPacketsCount() {
        return mErrorCount;
    }

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }
}
