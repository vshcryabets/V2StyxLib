package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;

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

    public ConnectionWithExport(Credentials credentials, IChannelDriver driver, ClientsRepo clientsRepo) {
        super(credentials, driver, clientsRepo);
    }

    @Override
    public String getProtocol() {
        return TCPDualLinkServerManager.PROTOCOL;
    }
    public void export(IVirtualStyxFile root) {
        mExportedRoot = root;
    }

    @Override
    public boolean connect() throws IOException, StyxException,
            InterruptedException, TimeoutException {
        boolean result = super.connect();
        mExportProcessor = new TMessagesProcessor(getConnectionDetails(), mExportedRoot,
                mClientsRepo);
        mDriver.setTMessageHandler(mExportProcessor);
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
