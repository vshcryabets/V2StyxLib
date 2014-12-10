package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.utils.FIDPoll;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.utils.MessageTagPoll;
import com.v2soft.styxlib.utils.Polls;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class RMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {
    protected int mReceivedCount, mErrorCount;
    protected String mTag;

    protected Polls mPolls;

    public RMessagesProcessor(String tag, Polls polls) {
        super();
        mTag = tag;
        mPolls = polls;
    }

    @Override
    public void addClient(ClientDetails state) {

    }

    @Override
    public void removeClient(ClientDetails state) {

    }

    @Override
    public void processPacket(StyxMessage message, ClientDetails target) throws IOException {
        mReceivedCount++;
        int tag = message.getTag();
        if (!mPolls.getMessagesMap().containsKey(tag)) {
            // we didn't send T message with such tag, so ignore this R message
            System.err.printf("%d\tGot (%s) unknown R message from client %s\n", System.currentTimeMillis(),
                    mTag,
                    target.toString());
            return;
        }
        final StyxTMessage tMessage = mPolls.getMessagesMap().get(tag);
        // TODO i'm not sure that this is proper place for that logic
        if (tMessage.getType() == MessageType.Tclunk ||
                tMessage.getType() == MessageType.Tremove) {
            mPolls.releaseFID((StyxTMessageFID) tMessage);
        }
        try {
            tMessage.setAnswer(message);
        } catch (StyxException e) {
            e.printStackTrace();
        }
        if (message.getType() == MessageType.Rerror) {
            mErrorCount++;
        }
        mPolls.releaseTag(tag);
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
