package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessageTransmitter;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.StyxServerManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPDualLinkServerManager extends StyxServerManager {

    private static final String DUAL_LINK_PROTO = "9P2000_2VDL";
    protected RMessagesProcessor mReverseAnswerProcessor;
    protected TMessageTransmitter mReverseTransmitter;

    public TCPDualLinkServerManager(InetAddress address, int port, boolean ssl, IVirtualStyxFile root) throws IOException {
        super(root,
                Arrays.asList(new TCPServerChannelDriver(
                        address, port, ssl)));
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
        Connection connection = new Connection(credentials, driver,
                mReverseAnswerProcessor, mReverseTransmitter, client);
        return connection;
    }
}
