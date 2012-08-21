package com.v2soft.styxlib.library.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.StyxClientConnection.ActiveFids;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;

/**
 * COdec factory for MINA
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxCodecFactory implements ProtocolCodecFactory {
    private StyxEncoder mEncoder;
    private StyxDecoder mDecoder;
    private StyxCodecFactory.ActiveTags mActiveTags;
    private Map<Integer, StyxTMessage> mMessages;    
    
    public StyxCodecFactory(int iounit, ActiveFids fids) {
        mActiveTags = new ActiveTags();
        mMessages = new HashMap<Integer, StyxTMessage>();
        mEncoder = new StyxEncoder(iounit, mMessages, mActiveTags);
        mDecoder = new StyxDecoder(iounit, mMessages, mActiveTags, fids);
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
        return mDecoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
        return mEncoder;
    }
    
    public class ActiveTags {
        private LinkedList<Integer> mAvailableTags = new LinkedList<Integer>();
        private int mLastTag = 0;
        private Object mSync = new Object();

        public int getTag()
        {
            synchronized (mSync)
            {
                if (!mAvailableTags.isEmpty())
                    return mAvailableTags.poll();

                mLastTag++;
                if (mLastTag > Consts.MAXUSHORT)
                    mLastTag = 0;
                return mLastTag;
            }
        }

        public boolean releaseTag(int tag)
        {
            synchronized (mSync)
            {
                if (tag == StyxMessage.NOTAG)
                    return false;
                return mAvailableTags.add(tag);
            }
        }
    }

}
