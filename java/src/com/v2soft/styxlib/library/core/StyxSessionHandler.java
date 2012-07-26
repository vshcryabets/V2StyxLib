package com.v2soft.styxlib.library.core;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.v2soft.styxlib.library.messages.base.StyxMessage;

/**
 * 
 * @author vshcryabets@gmail.com
 *
 */
public class StyxSessionHandler extends IoHandlerAdapter {
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass());
    
    @Override
    public void messageReceived(IoSession session, Object message) {
        logger.info("Message received in the client..");
        logger.info("Message is: " + message.toString());
        System.out.println(message.toString());
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }
}
