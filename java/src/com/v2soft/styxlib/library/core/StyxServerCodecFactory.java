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
public class StyxServerCodecFactory implements ProtocolCodecFactory {
    private StyxServerEncoder mEncoder;
    private StyxServerDecoder mDecoder;
    
    public StyxServerCodecFactory(int iounit) {
        mEncoder = new StyxServerEncoder(iounit);
        mDecoder = new StyxServerDecoder(iounit);
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
