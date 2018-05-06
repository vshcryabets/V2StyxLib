package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPServerManager extends StyxServerManager {

    public TCPServerManager(InetAddress address, int port, IVirtualStyxFile root) throws IOException {
        super(root, new IChannelDriver[]{new TCPServerChannelDriver(address, port)});
    }
}
