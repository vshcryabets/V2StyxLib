package com.v2soft.styxlib.library;

import com.v2soft.styxlib.StyxClientConnection;
import com.v2soft.styxlib.library.core.TMessagesProcessor;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
    protected TMessagesProcessor mExportProcessor;

    @Override
    public String getProtocol() {
        return TCPDualLinkServerManager.PROTOCOL;
    }
    public void export(IVirtualStyxFile root) {
        mExportedRoot = root;
    }

    @Override
    public boolean connect(IChannelDriver driver, Credentials credentials) throws IOException, StyxException,
            InterruptedException, TimeoutException {
        boolean result = super.connect(driver, credentials);
        mExportProcessor = new TMessagesProcessor(getConnectionDetails(), mExportedRoot);
        driver.setTMessageHandler(mExportProcessor);
        return result;
    }

    @Override
    public void close() throws IOException {
        if ( mExportProcessor != null ) {
            mExportProcessor.close();
            mExportProcessor = null;
        }
        super.close();
    }
}
