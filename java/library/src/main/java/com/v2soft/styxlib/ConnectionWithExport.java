package com.v2soft.styxlib;

import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Connection with export.
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 *
 */
public class ConnectionWithExport extends Connection {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    protected IVirtualStyxFile mExportedRoot = null;
    protected TMessagesProcessor mExportProcessor;


    public ConnectionWithExport(Credentials credentials, IChannelDriver driver) {
        super(credentials, driver);
    }

    public ConnectionWithExport() {
        super();
    }

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
