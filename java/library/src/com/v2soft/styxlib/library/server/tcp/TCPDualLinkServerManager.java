package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.IClient;
import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.Credentials;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPDualLinkServerManager extends TCPServerManager {

    private static final String DUAL_LINK_PROTO = "9P2000_2VDL";

    public TCPDualLinkServerManager(InetAddress address, int port, boolean ssl, IVirtualStyxFile root) throws IOException {
        super(address, port, ssl, root);
    }

    @Override
    public String getProtocol() {
        return DUAL_LINK_PROTO;
    }

    public IClient getReverseConnectionForClient(ClientState client, Credentials credentials) {
        StyxClientConnection connection = new StyxClientConnection(credentials);
        IChannelDriver driver = client.getDriver();
        connection.setDriver(driver);
//        connection.connect(mDrivers);
        return connection;
    }
}
