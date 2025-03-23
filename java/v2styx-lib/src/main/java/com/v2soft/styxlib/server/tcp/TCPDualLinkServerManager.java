package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessageTransmitter;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.StyxServerManager;

import java.util.List;

/**
 * Created by V.Shcryabets on 5/22/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPDualLinkServerManager extends StyxServerManager {

    private static final String DUAL_LINK_PROTO = "9P2000_2VDL";
    protected RMessagesProcessor mReverseAnswerProcessor;
    protected TMessageTransmitter mReverseTransmitter;

    public TCPDualLinkServerManager(IVirtualStyxFile root,
                                    List<IChannelDriver> drivers,
                                    ClientsRepo clientsRepo) {
        super(root, drivers, clientsRepo);
    }

    @Override
    public String getProtocol() {
        return DUAL_LINK_PROTO;
    }

    public synchronized  IClient getReverseConnectionForClient(int clientId, Credentials credentials) {
        if ( mReverseAnswerProcessor == null ) {
            mReverseAnswerProcessor = new RMessagesProcessor("RC"+clientId, mClientsRepo);
            mReverseTransmitter = new TMessageTransmitter(null, mClientsRepo);
        }
        var driver = mClientsRepo.getChannelDriver(clientId);
        return new Connection(credentials,
                driver,
                mReverseAnswerProcessor,
                mReverseTransmitter,
                mClientsRepo);
    }
}
