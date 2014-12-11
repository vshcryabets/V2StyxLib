package com.v2soft.styxlib;

import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

/**
 * Created by V.Shcryabets on 4/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface ILogListener {
    public void onMessageReceived(IChannelDriver driver, ClientDetails clientDetails, StyxMessage message);
    public void onMessageTransmited(IChannelDriver driver, ClientDetails clientDetails, StyxMessage message);
    public void onException(IChannelDriver driver, Throwable err);
}
