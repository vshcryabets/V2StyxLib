package com.v2soft.styxlib.library.core;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.StyxClientConnection.ActiveFids;
import com.v2soft.styxlib.library.io.StyxByteBufferWriteable;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ObjectsPoll;
import com.v2soft.styxlib.library.types.ObjectsPoll.ObjectsPollFactory;

/**
 * Styx message encoder for Mina framework
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxEncoder implements ProtocolEncoder, ObjectsPollFactory<StyxByteBufferWriteable> {
    private int mTransmitedCount, mBuffersAllocated;
    private ObjectsPoll<StyxByteBufferWriteable> mBufferPoll;
    private StyxCodecFactory.ActiveTags mActiveTags;
    private int mIOBufferSize;
    private Map<Integer, StyxTMessage> mMessages;

    public StyxEncoder(int io_unit, Map<Integer, StyxTMessage> messagesMap,
            StyxCodecFactory.ActiveTags activeTags) {
        mMessages = messagesMap;
        mIOBufferSize = io_unit;
        mActiveTags = activeTags;
        resetStatistics();
        mBufferPoll = new ObjectsPoll<StyxByteBufferWriteable>(this);
    }

    private void resetStatistics() {
        mTransmitedCount = 0;
        mBuffersAllocated = 0;
    }

    @Override
    public void dispose(IoSession arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void encode(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2)
            throws Exception {
        StyxTMessage message = (StyxTMessage) arg1;
        if ( Config.LOG_TMESSAGES) {
            System.out.println("Send message "+message.toString());
        }
        int tag = StyxMessage.NOTAG;
        if ( message.getType() != MessageType.Tversion ) {
            tag = mActiveTags.getTag();
        }
        message.setTag((short) tag);
        mMessages.put(tag, message);

        StyxByteBufferWriteable buffer = mBufferPoll.get();
        message.writeToBuffer(buffer);
        final IoBuffer inbuf = buffer.getBuffer();
        inbuf.flip();
        arg2.write(inbuf);
        mTransmitedCount++;        
    }
    

    
    public int getTransmitedCount() {return mTransmitedCount;}
    public int getAllocationCount() {return mBuffersAllocated;}
    
    @Override
    public StyxByteBufferWriteable create() {
        mBuffersAllocated++;
        return new StyxByteBufferWriteable(mIOBufferSize);
    }
}
