package com.v2soft.styxlib.library.core;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;

import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.utils.Log;

/**
 * Styx messages decoder for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxDecoder implements ProtocolDecoder {
    private int mIOUnit;
    
    public StyxDecoder(int ioUnit) {
        mIOUnit = ioUnit;
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
        System.out.println(message.toString());
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

}
