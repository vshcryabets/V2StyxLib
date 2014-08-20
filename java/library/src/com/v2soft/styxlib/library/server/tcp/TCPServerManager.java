package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPServerManager extends StyxServerManager {

    public TCPServerManager(InetAddress address, int port, boolean ssl, IVirtualStyxFile root) throws IOException {
        super(root);
        addDriver(prepareDriver(address, port, ssl));
    }

    private IChannelDriver prepareDriver(InetAddress address, int port, boolean ssl) throws IOException {
        TCPChannelDriver driver = new TCPServerChannelDriver(address, port, ssl, getIOUnit());
        return driver;
    }
}
