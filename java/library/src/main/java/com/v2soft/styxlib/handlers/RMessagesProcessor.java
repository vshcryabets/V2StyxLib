package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.utils.AbstractPoll;
import com.v2soft.styxlib.library.utils.FIDPoll;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.library.utils.MessageTagPoll;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class RMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {
    protected int mReceivedCount, mErrorCount;
    protected Map<Integer, StyxTMessage> mMessagesMap;
    protected String mTag;

    protected MessageTagPoll mActiveTags;
    protected FIDPoll mFids = new FIDPoll();

    public RMessagesProcessor(String tag) {
        super();
        mTag = tag;
        mActiveTags = new MessageTagPoll();
        mMessagesMap = new HashMap<Integer, StyxTMessage>();
    }

    @Override
    public void addClient(ClientDetails state) {

    }

    @Override
    public void removeClient(ClientDetails state) {

    }

    public FIDPoll getFIDPoll() {
        return mFids;
    }

    public MessageTagPoll getTagPoll() {
        return mActiveTags;
    }

    public Map<Integer, StyxTMessage> getMessagesMap() {
        return mMessagesMap;
    }

    @Override
    public void processPacket(StyxMessage message, ClientDetails target) throws IOException {
        mReceivedCount++;
        int tag = message.getTag();
        if (!mMessagesMap.containsKey(tag)) {
            // we didn't send T message with such tag, so ignore this R message
            System.err.printf("%d\tGot (%s) unknown R message from client %s\n", System.currentTimeMillis(),
                    mTag,
                    target.toString());
            return;
        }
        final StyxTMessage tMessage = mMessagesMap.get(tag);
        // TODO i'm not sure that this is proper place for that logic
        if (tMessage.getType() == MessageType.Tclunk ||
                tMessage.getType() == MessageType.Tremove) {
            mFids.release(( (StyxTMessageFID) tMessage ).getFID());
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
}
