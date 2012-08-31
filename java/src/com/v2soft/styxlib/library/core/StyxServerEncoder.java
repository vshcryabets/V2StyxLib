package com.v2soft.styxlib.library.core;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.io.StyxByteBufferWriteable;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.types.ObjectsPoll;
import com.v2soft.styxlib.library.types.ObjectsPoll.ObjectsPollFactory;

/**
 * Styx message encoder for Mina framework
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxServerEncoder implements ProtocolEncoder, ObjectsPollFactory<StyxByteBufferWriteable> {
    protected int mTransmitedCount, mBuffersAllocated;
    protected ObjectsPoll<StyxByteBufferWriteable> mBufferPoll;
    protected int mIOBufferSize;

    public StyxServerEncoder(int io_unit) {
        mIOBufferSize = io_unit;
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
        StyxMessage message = (StyxMessage) arg1;
        if ( Config.LOG_TMESSAGES) {
            System.out.println("Send message "+message.toString());
        }
        StyxByteBufferWriteable buffer = mBufferPoll.get();
        message.writeToBuffer(buffer);
        final IoBuffer inbuf = buffer.getBuffer();
        inbuf.flip();
        arg2.write(inbuf);
        mTransmitedCount++;
    }

    /**
     * 
     * @return number of transmitted packets
     */
    public int getTransmitedCount() {return mTransmitedCount;}
    /**
     * 
     * @return number of allocated memory buffers for packets
     */
    public int getAllocationCount() {return mBuffersAllocated;}
    
    @Override
    public StyxByteBufferWriteable create() {
        mBuffersAllocated++;
        return new StyxByteBufferWriteable(mIOBufferSize);
    }
}
