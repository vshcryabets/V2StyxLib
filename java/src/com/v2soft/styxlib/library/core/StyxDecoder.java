package com.v2soft.styxlib.library.core;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Styx messages decoder for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxDecoder implements ProtocolDecoder {

    @Override
    public void decode(IoSession arg0, IoBuffer arg1, ProtocolDecoderOutput arg2)
            throws Exception {
        if ( arg1.limit() < 4 ) {
            return;
        }
        // TODO Auto-generated method stub

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

}
