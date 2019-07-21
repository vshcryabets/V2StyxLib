package com.v2soft.styxlib;

import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.TMessageTransmitter;
import com.v2soft.styxlib.types.Credentials;
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

    public static class Builder extends Connection.Builder {
        @Override
        public ConnectionWithExport build() {
            return new ConnectionWithExport(mCredentials, mDriver, mAnswerProcessor, mTransmitter, mClientDetails);
        }

        @Override
        public Builder setDriver(IChannelDriver driver) {
            super.setDriver(driver);
            return this;
        }
    }

    public ConnectionWithExport(Credentials credentials, IChannelDriver driver, RMessagesProcessor answerProcessor,
                                TMessageTransmitter transmitter, ClientDetails recepient) {
        super(credentials, driver, answerProcessor, transmitter, recepient);
    }

    @Override
    public String getProtocol() {
        return TCPDualLinkServerManager.PROTOCOL;
    }
    public void export(IVirtualStyxFile root) {
        mExportedRoot = root;
    }

//    public boolean connect(IChannelDriver driver, Credentials credentials,
//                           RMessagesProcessor answerProcessor,
//                           TMessageTransmitter transmitter, ClientDetails recepient)
//            throws InterruptedException, StyxException, TimeoutException, IOException {
//        mExportProcessor = new TMessagesProcessor("clientTH", getConnectionDetails(), mExportedRoot);
//        driver.setTMessageHandler(mExportProcessor);
//        return super.connect(driver, credentials, answerProcessor, transmitter, recepient);
//    }

    @Override
    public void close() throws IOException {
        if ( mExportProcessor != null ) {
            mExportProcessor.close();
            mExportProcessor = null;
        }
        super.close();
    }
}
