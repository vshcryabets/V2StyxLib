package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

/**
 * Created by vshcryabets on 12/8/14.
 */
public class TestLogListener implements ILogListener {
    private String mPrefix;

    TestLogListener(String prefix) {
        mPrefix = prefix;
    }

    @Override
    public void onMessageReceived(IChannelDriver driver, ClientDetails clientDetails, StyxMessage message) {
        long time = System.currentTimeMillis();
        System.out.println(String.format("%d %s GET %s client %s message %s %d", time, mPrefix,
                driver.toString(),
                clientDetails.toString(),
                message.getType().toString(),
                message.getTag()));
    }

    @Override
    public void onMessageTransmited(IChannelDriver driver, ClientDetails clientDetails, StyxMessage message) {
        long time = System.currentTimeMillis();
        System.out.println(String.format("%d %s SENT %s client %s message %s", time, mPrefix,
                driver.toString(),
                clientDetails.toString(),
                message.toString()));
    }

    @Override
    public void onException(IChannelDriver driver, Throwable err) {

    }
}
