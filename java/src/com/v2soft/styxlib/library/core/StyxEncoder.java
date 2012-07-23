package com.v2soft.styxlib.library.core;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.v2soft.styxlib.library.io.StyxByteBufferWriteable;
import com.v2soft.styxlib.library.messages.base.StyxMessage;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxEncoder implements ProtocolEncoder {

    @Override
    public void dispose(IoSession arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void encode(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2)
            throws Exception {
        StyxMessage message = (StyxMessage) arg1;
        StyxByteBufferWriteable buffer = new StyxByteBufferWriteable(message.getBinarySize());
        message.writeToBuffer(buffer);
        IoBuffer inbuffer = buffer.getBuffer();
        inbuffer.flip();
        arg2.write(inbuffer);
    }

}
