package com.v2soft.styxlib.library.server;

import com.v2soft.styxlib.library.messages.base.StyxMessage;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IClientChannelDriver extends IChannelDriver, IMessageTransmitter {
    boolean isConnected();
}
