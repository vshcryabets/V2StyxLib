package com.v2soft.styxlib.library;

import com.v2soft.styxlib.StyxClientConnection;
import com.v2soft.styxlib.library.core.MessengerWithExport;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;

/**
 * Dual link client connection.
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class DualLinkClientConnection extends StyxClientConnection {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    protected IVirtualStyxFile mExportedRoot = null;

    @Override
    public String getProtocol() {
        return TCPDualLinkServerManager.PROTOCOL;
    }
    public void export(IVirtualStyxFile root) {
        mExportedRoot = root;
    }

    @Override
    protected IMessageTransmitter initMessenger(IChannelDriver driver) throws IOException {
        MessengerWithExport result = new MessengerWithExport(driver, this);
        result.start(true, getIOBufSize());

        if ( mExportedRoot != null ) {
            result.export(mExportedRoot, mDetails);
        }
        return result;
    }
}
