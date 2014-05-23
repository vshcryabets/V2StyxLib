package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.v2soft.styxlib.library.core.IMessageProcessor;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;


/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientBalancer implements IMessageProcessor {
    private MessagesProcessor mHandler;

    public ClientBalancer(int iounit, IVirtualStyxFile root, String protocol) {
        mHandler = new MessagesProcessor(iounit, root, protocol);
    }

    public void addClient(ClientState state) {
        mHandler.addClient(state);
    }
    public void removeClient(ClientState state) {
        mHandler.removeClient(state);
    }
    public void processPacket(ClientState client, StyxMessage message) {
        try {
            mHandler.processPacket(client, message);
        } catch (IOException e) {
            // TODO do something
            e.printStackTrace();
        }
    }
}
