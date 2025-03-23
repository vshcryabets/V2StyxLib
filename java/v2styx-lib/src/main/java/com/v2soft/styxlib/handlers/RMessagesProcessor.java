package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.server.ClientsRepo;

import java.util.Map;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class RMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {
    protected int mReceivedCount, mErrorCount;
    protected String mTag;
    protected ClientsRepo mClientsRepo;

    public RMessagesProcessor(String tag, ClientsRepo clientsRepo) {
        super();
        mTag = tag;
        mClientsRepo = clientsRepo;
    }

    @Override
    public void onClientRemoved(int clientId) {

    }

    @Override
    public void processPacket(StyxMessage message, int clientId) {
        mReceivedCount++;
        int tag = message.getTag();
        final var polls = mClientsRepo.getPolls(clientId);
        final Map<Integer, StyxTMessage> clientMessagesMap = polls.getMessagesMap();
        if (!clientMessagesMap.containsKey(tag)) {
            // we didn't send T message with such tag, so ignore this R message
            System.err.printf("%d\tGot (%s) unknown R message from client %d\n", System.currentTimeMillis(),
                    mTag,
                    clientId);
            return;
        }
        final StyxTMessage tMessage = clientMessagesMap.get(tag);
        // TODO i'm not sure that this is proper place for that logic
        if (tMessage.getType() == MessageType.Tclunk ||
                tMessage.getType() == MessageType.Tremove) {
            polls.releaseFID((StyxTMessageFID) tMessage);
        }
        try {
            tMessage.setAnswer(message);
        } catch (StyxException e) {
            e.printStackTrace();
        }
        if (message.getType() == MessageType.Rerror) {
            mErrorCount++;
        }
        polls.releaseTag(tag);
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
