package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.IClient;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPTwoWayServerManager extends TCPServerManager {

    public TCPTwoWayServerManager(InetAddress address, int port, boolean ssl, IVirtualStyxFile root) throws IOException {
        super(address, port, ssl, root);
    }

    public IClient getClient(ClientState client) {
        // 1. construct IClientChannelDriver

        // 2. create new instance of StyxClientConnection
        return null;
    }
}
