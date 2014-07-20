package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.ClientState;

import java.io.IOException;
import java.util.Map;

/**
 * Created by mrco on 7/20/14.
 */
public class RMessagesProcessor implements IMessageProcessor {
    protected ILogListener mLogListener;
    protected int mReceivedCount, mErrorCount;
    protected Map<Integer, StyxTMessage> mMessagesMap;
    protected Messenger.StyxMessengerListener mListener;
    protected Messenger.ActiveTags mActiveTags;

    @Override
    public void addClient(ClientState state) {

    }

    @Override
    public void removeClient(ClientState state) {

    }

    @Override
    public void processPacket(StyxMessage message) throws IOException {
        if ( mLogListener != null ) {
            mLogListener.onMessageReceived(message);
        }
        mReceivedCount++;
        int tag = message.getTag();
        if (!mMessagesMap.containsKey(tag)) // we didn't send T message with such tag, so ignore this R message
            return;
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
        mActiveTags.releaseTag(tag);
    }

    @Override
    public int getReceivedPacketsCount() {
        return mReceivedCount;
    }

    @Override
    public int getReceivedErrorPacketsCount() {
        return mErrorCount;
    }

    @Override
    public void close() throws IOException {

    }

    public void setmLogListener(ILogListener mLogListener) {
        this.mLogListener = mLogListener;
    }

    public void setmMessagesMap(Map<Integer, StyxTMessage> mMessagesMap) {
        this.mMessagesMap = mMessagesMap;
    }

    public void setmListener(Messenger.StyxMessengerListener mListener) {
        this.mListener = mListener;
    }

    public void setmActiveTags(Messenger.ActiveTags mActiveTags) {
        this.mActiveTags = mActiveTags;
    }

}