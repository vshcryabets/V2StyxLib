package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.utils.FIDPoll;
import com.v2soft.styxlib.utils.Polls;

public interface ClientsRepo {
    int addClient(ClientDetails client);
    void removeClient(int id);
    ClientDetails getClient(int id);

    IVirtualStyxFile getAssignedFile(int clientId, long fid) throws StyxErrorMessageException;
    IMessageTransmitter getDriver(int clientId);

    void closeFile(int clientId, long fid) throws StyxErrorMessageException;
    FIDPoll getFidPoll(int clientId);
    Polls getPolls(int clientId);
    IChannelDriver getChannelDriver(int clientId);
}
