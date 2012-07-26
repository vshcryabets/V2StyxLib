package com.v2soft.styxlib.library.core;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * COdec factory for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxCodecFactory implements ProtocolCodecFactory {
    private StyxEncoder mEncoder;
    private StyxDecoder mDecoder;
    
    public StyxCodecFactory(int iounit) {
        mEncoder = new StyxEncoder();
        mDecoder = new StyxDecoder(iounit);
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
        return mDecoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
        return mEncoder;
    }

}
