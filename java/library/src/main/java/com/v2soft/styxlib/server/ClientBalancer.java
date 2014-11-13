package com.v2soft.styxlib.server;

import com.v2soft.styxlib.library.core.IMessageProcessor;
import com.v2soft.styxlib.library.core.TMessagesProcessor;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;


/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientBalancer implements IMessageProcessor {
    private IMessageProcessor mHandler;

    public ClientBalancer(ConnectionDetails details, IVirtualStyxFile root) {
        mHandler = new TMessagesProcessor(details, root);
    }
    @Override
    public void addClient(ClientDetails state) {
        mHandler.addClient(state);
    }
    @Override
    public void removeClient(ClientDetails state) {
        mHandler.removeClient(state);
    }
    @Override
    public void processPacket(StyxMessage message, ClientDetails transmitter) throws IOException {
        mHandler.processPacket(message, transmitter);
    }

    @Override
    public int getReceivedPacketsCount() {
        return 0;
    }

    @Override
    public int getReceivedErrorPacketsCount() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        mHandler.close();
    }
}
