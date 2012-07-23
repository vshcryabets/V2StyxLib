package com.v2soft.styxlib.library.core;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxCodecFactory implements ProtocolCodecFactory {
    private StyxEncoder mEncoder;
    private StyxDecoder mDecoder;
    
    public StyxCodecFactory() {
        mEncoder = new StyxEncoder();
        mDecoder = new StyxDecoder();
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
