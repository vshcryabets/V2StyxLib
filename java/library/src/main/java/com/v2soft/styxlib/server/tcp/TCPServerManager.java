package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
// TODO this class looks useless, probably we can remove it
public class TCPServerManager extends StyxServerManager {
    public TCPServerManager(String address, int port, IVirtualStyxFile root) {
        super(root);
        addDriver(new TCPServerChannelDriver(address, port));
    }
}
