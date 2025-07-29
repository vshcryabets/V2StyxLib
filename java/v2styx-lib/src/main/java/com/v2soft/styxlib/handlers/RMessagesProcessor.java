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
public class RMessagesProcessor extends QueueMessagesProcessor {
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
        try {
            final var polls = mClientsRepo.getPolls(clientId);
            polls.assignAnswer(tag, message);
            // TODO i'm not sure that this is proper place for that logic
            // TODO restore before merge
//            if (tMessage.type == MessageType.Tclunk ||
//                    tMessage.type == MessageType.Tremove) {
//                polls.releaseFID((StyxTMessageFID) tMessage);
//            }
            polls.releaseTag(tag);
        } catch (StyxException e) {
            e.printStackTrace();
        }
        if (message.getType() == MessageType.Rerror) {
            mErrorCount++;
        }
    }
}
