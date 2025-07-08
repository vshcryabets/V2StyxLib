package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.utils.FIDPoll;
import com.v2soft.styxlib.utils.Polls;

public interface ClientsRepo {
    int addClient(ClientDetails client);
    void removeClient(int id);
    ClientDetails getClient(int id) throws StyxUnknownClientIdException;

    IVirtualStyxFile getAssignedFile(int clientId, long fid) throws StyxErrorMessageException, StyxUnknownClientIdException;
    IMessageTransmitter getDriver(int clientId) throws StyxUnknownClientIdException;

    void closeFile(int clientId, long fid) throws StyxErrorMessageException, StyxUnknownClientIdException;
    FIDPoll getFidPoll(int clientId) throws StyxUnknownClientIdException;
    Polls getPolls(int clientId) throws StyxUnknownClientIdException;
    IChannelDriver<?> getChannelDriver(int clientId) throws StyxUnknownClientIdException;
}
