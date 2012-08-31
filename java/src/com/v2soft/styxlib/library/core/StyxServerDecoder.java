package com.v2soft.styxlib.library.core;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.messages.base.StyxMessage;

/**
 * Styx messages decoder for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxServerDecoder extends CumulativeProtocolDecoder {
    protected int mIOUnit;
    protected int mReceivedCount, mErrorCount;

    public StyxServerDecoder(int ioUnit) {
        mIOUnit = ioUnit;
    }

    protected synchronized void processIncomingMessage(StyxMessage message, 
            ProtocolDecoderOutput arg2) 
            throws StyxException {
        arg2.write(message);
    }

    public void setIOUnit(int value) {
        mIOUnit = value;
    }

    public int getReceivedCount() {return mReceivedCount;}
    public int getErrorsCount() {return mErrorCount;}

    @Override
    protected boolean doDecode(IoSession arg0, IoBuffer arg1,
            ProtocolDecoderOutput arg2) throws Exception {
        if ( arg1.limit() < 4 ) {
            return false;
        }
        arg1.order(ByteOrder.LITTLE_ENDIAN);
        int position = arg1.position();
        int packetSize = arg1.getInt();
        arg1.position(position);
        if ( packetSize > arg1.limit() ) {
            // not enough data to decode
            return false;
        }
        try {
            final StyxByteBufferReadable readable = new StyxByteBufferReadable(arg1);
            final StyxMessage message = StyxMessage.factory(readable, mIOUnit);
            processIncomingMessage(message, arg2);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        return false;
    }

}
