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

    public ConnectionWithExport(Connection.Configuration configuration) {
        super(configuration);
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
        mConfiguration.requestProcessor = new TMessagesProcessor(getConnectionDetails(), mExportedRoot,
                mConfiguration.clientsRepo);
        return super.connect();
    }

    @Override
    public void close() throws IOException {
        super.close();
        mConfiguration.requestProcessor.close();
    }
}
