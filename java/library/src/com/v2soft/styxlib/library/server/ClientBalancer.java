package com.v2soft.styxlib.library.server;

import com.v2soft.styxlib.library.core.IMessageProcessor;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.IOException;


/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientBalancer implements IMessageProcessor {
    private IMessageProcessor mHandler;

    public ClientBalancer(int iounit, IVirtualStyxFile root, String protocol) {
        mHandler = new TMessagesProcessor(iounit, root, protocol);
    }
    @Override
    public void addClient(ClientState state) {
        mHandler.addClient(state);
    }
    @Override
    public void removeClient(ClientState state) {
        mHandler.removeClient(state);
    }
    @Override
    public void processPacket(StyxMessage message) throws IOException {
        mHandler.processPacket(message);
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
