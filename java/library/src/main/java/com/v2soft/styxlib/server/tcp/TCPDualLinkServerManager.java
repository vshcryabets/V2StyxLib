package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.Connection;
import com.v2soft.styxlib.IClient;
import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.server.TMessageTransmitter;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.types.Credentials;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPDualLinkServerManager extends TCPServerManager {

    private static final String DUAL_LINK_PROTO = "9P2000_2VDL";
    protected RMessagesProcessor mReverseAnswerProcessor;
    protected TMessageTransmitter mReverseTransmitter;

    public TCPDualLinkServerManager(InetAddress address, int port, boolean ssl, IVirtualStyxFile root) throws IOException {
        super(address, port, ssl, root);
    }

    @Override
    public String getProtocol() {
        return DUAL_LINK_PROTO;
    }

    public synchronized  IClient getReverseConnectionForClient(ClientDetails client, Credentials credentials) {
        if ( mReverseAnswerProcessor == null ) {
            mReverseAnswerProcessor = new RMessagesProcessor("RC"+client.toString());
            mReverseTransmitter = new TMessageTransmitter(null);
        }
        IChannelDriver driver = client.getDriver();
        driver.setRMessageHandler(mReverseAnswerProcessor);
        Connection connection = new Connection(credentials, driver,
                mReverseAnswerProcessor, mReverseTransmitter, client);
        return connection;
    }
}
