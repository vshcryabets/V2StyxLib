package com.v2soft.styxlib;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;

/**
 * Created by V.Shcryabets on 4/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface ILogListener {
    public void onMessageReceived(IChannelDriver driver, ClientState client, StyxMessage message);
    public void onMessageTransmited(IChannelDriver driver, ClientState client, StyxMessage message);
    public void onException(IChannelDriver driver, Throwable err);
}
