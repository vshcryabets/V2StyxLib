package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.utils.FIDPoll;
import com.v2soft.styxlib.utils.Polls;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientsRepoImpl implements ClientsRepo {
    private final HashMap<Integer, ClientDetails> mClients;
    private final AtomicInteger mIdCounter = new AtomicInteger(1);

    public ClientsRepoImpl() {
        mClients = new HashMap<>();
    }

    @Override
    public int addClient(ClientDetails client) {
        int id = mIdCounter.getAndIncrement();
        mClients.put(id, client);
        client.setId(id);
        return id;
    }

    @Override
    public void removeClient(int id) {
        mClients.remove(id);
    }

    @Override
    public ClientDetails getClient(int id) {
        return mClients.get(id);
    }

    @Override
    public IVirtualStyxFile getAssignedFile(int clientId, long fid) throws StyxErrorMessageException {
        return mClients.get(clientId).getAssignedFile(fid);
    }

    @Override
    public IMessageTransmitter getDriver(int clientId) {
        return mClients.get(clientId).getDriver();
    }

    @Override
    public void closeFile(int clientId, long fid) {
        mClients.get(clientId).closeFile(fid);
    }

    @Override
    public FIDPoll getFidPoll(int clientId) {
        return mClients.get(clientId).getPolls().getFIDPoll();
    }

    @Override
    public Polls getPolls(int clientId) {
        return mClients.get(clientId).getPolls();
    }

    @Override
    public IChannelDriver getChannelDriver(int clientId) {
        return mClients.get(clientId).getDriver();
    }
}
