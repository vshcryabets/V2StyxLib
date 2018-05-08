package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;

/**
 * Class that processes RMessages (i.e answer from server).
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class RMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {

    public RMessagesProcessor(String tag) {
        super(tag);
    }

    @Override
    public void processPacket(StyxMessage message, ClientDetails client) throws StyxException {
        mHandledPackets++;
        int tag = message.getTag();
        final StyxTMessage tMessage = client.getPolls().getTMessage(tag);
        if (tMessage == null) {
            // we didn't send T message with such tag, so ignore this R message
            throw new StyxException(String.format("RMP(%s) got unknown R message from server %s",
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
            mErrorPackets++;
        }
        client.getPolls().releaseTag(tag);
    }
}
