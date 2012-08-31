package com.v2soft.styxlib.library.core;

import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

/**
 * Styx message encoder for Mina framework
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxEncoder extends StyxServerEncoder {
    private StyxCodecFactory.ActiveTags mActiveTags;
    private Map<Integer, StyxTMessage> mMessages;

    public StyxEncoder(int io_unit, Map<Integer, StyxTMessage> messagesMap,
            StyxCodecFactory.ActiveTags activeTags) {
        super(io_unit);
        mMessages = messagesMap;
        mActiveTags = activeTags;
    }

    @Override
    public void encode(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2)
            throws Exception {
        StyxTMessage message = (StyxTMessage) arg1;
        int tag = StyxMessage.NOTAG;
        if ( message.getType() != MessageType.Tversion ) {
            tag = mActiveTags.getTag();
        }
        message.setTag((short) tag);
        mMessages.put(tag, message);
        super.encode(arg0, message, arg2);
    }
}
