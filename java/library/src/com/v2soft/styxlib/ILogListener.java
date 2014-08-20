package com.v2soft.styxlib;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;

/**
 * Created by V.Shcryabets on 4/3/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface ILogListener {
    public void onMessageReceived(StyxMessage message);
    public void onSendMessage(StyxMessage message);
    public void onException(Throwable err);
}
