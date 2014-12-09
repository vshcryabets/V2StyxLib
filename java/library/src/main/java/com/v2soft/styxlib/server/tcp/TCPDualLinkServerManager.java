package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.Connection;
import com.v2soft.styxlib.IClient;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;
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

    public IClient getReverseConnectionForClient(ClientDetails clientDetails, Credentials credentials) {
        IChannelDriver driver = clientDetails.getDriver();
        Connection connection = new Connection(credentials, driver);
        return connection;
    }
}
