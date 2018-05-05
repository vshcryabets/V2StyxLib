package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;

/**
 * Class that processes RMessages (i.e answer from server).
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class RMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {
    protected int mReceivedCount, mErrorCount;
    protected String mTag;

    public RMessagesProcessor(String tag) {
        super();
        mTag = tag;
    }

    @Override
    public void addClient(ClientDetails state) {
        // nothing to do
    }

    @Override
    public void removeClient(ClientDetails state) {
        // nothing to do
    }

    @Override
    public void processPacket(StyxMessage message, ClientDetails client) throws IOException, StyxException {
        mReceivedCount++;
        int tag = message.getTag();
        final StyxTMessage tMessage = client.getPolls().getTMessage(tag);
        if (tMessage == null) {
            // we didn't send T message with such tag, so ignore this R message
            throw new StyxException(String.format("Got (%s) unknown R message from client %s\n",
                    mTag,
                    client.toString()));
        }
        // TODO i'm not sure that this is proper place for that logic
        if (tMessage.getType() == MessageType.Tclunk ||
                tMessage.getType() == MessageType.Tremove) {
            client.getPolls().releaseFID((StyxTMessageFID) tMessage);
        }
        try {
            tMessage.setAnswer(message);
        } catch (StyxException e) {
            e.printStackTrace();
        }
        if (message.getType() == MessageType.Rerror) {
            mErrorCount++;
        }
        client.getPolls().releaseTag(tag);
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
