package com.v2soft.styxlib.library.core;

import java.util.Map;

import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.v2soft.styxlib.library.StyxClientConnection.ActiveFids;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

/**
 * Styx messages decoder for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxDecoder extends StyxServerDecoder {
    private Map<Integer, StyxTMessage> mMessages;
    private StyxCodecFactory.ActiveTags mActiveTags;
    private ActiveFids mActiveFIDs;

    public StyxDecoder(int ioUnit, Map<Integer, StyxTMessage> messagesMap,
            StyxCodecFactory.ActiveTags activeTags,
            ActiveFids fids) {
        super(ioUnit);
        mMessages = messagesMap;
        mActiveTags = activeTags;
        mActiveFIDs = fids;
    }

    @Override
    protected synchronized void processIncomingMessage(StyxMessage message, 
            ProtocolDecoderOutput arg2) 
            throws StyxException {
        int tag = message.getTag();
        if (!mMessages.containsKey(tag)) // we didn't send T message with such tag, so ignore this R message
            return;
        final StyxTMessage tMessage = mMessages.get(tag);
        if ( tMessage.getType() == MessageType.Tclunk || 
                tMessage.getType() == MessageType.Tremove) {
            mActiveFIDs.releaseFid(((StyxTMessageFID)tMessage).getFID());
        }
        tMessage.setAnswer(message);
        if ( message.getType() == MessageType.Rerror ) {
            mErrorCount++;
        }
        mMessages.remove(tag);
        mActiveTags.releaseTag(tag);
    }
}
