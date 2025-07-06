package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessageTransmitter;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.StyxServerManager;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPDualLinkServerManager extends StyxServerManager {

    private static final String DUAL_LINK_PROTO = "9P2000_2VDL";
    protected RMessagesProcessor mReverseAnswerProcessor;
    protected TMessageTransmitter mReverseTransmitter;

    public TCPDualLinkServerManager(StyxServerManager.Configuration configuration) {
        super(configuration);
    }

    @Override
    public String getProtocol() {
        return DUAL_LINK_PROTO;
    }

    public synchronized  IClient getReverseConnectionForClient(int clientId, Credentials credentials) {
        if ( mReverseAnswerProcessor == null ) {
            mReverseAnswerProcessor = new RMessagesProcessor("RC"+clientId, mConfiguration.di.getClientsRepo());
            mReverseTransmitter = new TMessageTransmitter(null, mConfiguration.di.getClientsRepo());
        }
        var driver = mConfiguration.di.getClientsRepo().getChannelDriver(clientId);
        return new Connection(new Connection.Configuration(
                credentials,
                driver,
                mConfiguration.di,
                mReverseAnswerProcessor,
                mReverseTransmitter
        ));
    }
}
