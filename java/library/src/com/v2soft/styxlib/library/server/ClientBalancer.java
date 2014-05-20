package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;


public class ClientBalancer {
    private MessagesProcessor mHandler;
//    protected Set<ClientState> mClients;

    public ClientBalancer(int iounit, IVirtualStyxFile root, String protocol) throws IOException {
        mHandler = new MessagesProcessor(iounit, root, protocol);
//        mClients = new HashSet<ClientState>();
    }

    public void addClient(ClientState state) {
//        mClients.add(state);
        mHandler.addClient(state);
    }
    public void removeClient(ClientState state) {
//        mClients.remove(state);
        mHandler.removeClient(state);
    }
    public void processPacket(ClientState client, StyxMessage message) throws IOException {
        mHandler.processPacket(client, message);
    }
}
