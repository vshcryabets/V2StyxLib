package com.v2soft.styxlib.library.core;

import java.nio.ByteOrder;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.v2soft.styxlib.library.StyxClientConnection.ActiveFids;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

/**
 * Styx messages decoder for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxDecoder implements ProtocolDecoder {
    private int mIOUnit;
    private Map<Integer, StyxTMessage> mMessages;
    private int mReceivedCount, mErrorCount;
    private StyxCodecFactory.ActiveTags mActiveTags;
    private ActiveFids mActiveFIDs;
    
    public StyxDecoder(int ioUnit, Map<Integer, StyxTMessage> messagesMap,
            StyxCodecFactory.ActiveTags activeTags,
            ActiveFids fids) {
        mIOUnit = ioUnit;
        mMessages = messagesMap;
        mActiveTags = activeTags;
        mActiveFIDs = fids;
    }

    @Override
    public void decode(IoSession arg0, IoBuffer arg1, ProtocolDecoderOutput arg2)
            throws Exception {
        if ( arg1.limit() < 4 ) {
            return;
        }
        arg1.order(ByteOrder.LITTLE_ENDIAN);
        int position = arg1.position();
        int packetSize = arg1.getInt();
        arg1.position(position);
        if ( packetSize < arg1.limit() ) {
            // not enough data to decode
            return;
        }
        final StyxByteBufferReadable readable = new StyxByteBufferReadable(arg1);
        final StyxMessage message = StyxMessage.factory(readable, mIOUnit);
        processIncomingMessage(message);
    }
    
    private synchronized void processIncomingMessage(StyxMessage message) 
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

    @Override
    public void dispose(IoSession arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
            throws Exception {
        // TODO Auto-generated method stub

    }
    
    public void setIOUnit(int value) {
        mIOUnit = value;
    }
    
    public int getReceivedCount() {return mReceivedCount;}
    public int getErrorsCount() {return mErrorCount;}

}
