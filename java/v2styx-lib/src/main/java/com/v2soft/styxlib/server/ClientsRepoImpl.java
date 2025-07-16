package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.utils.FIDPoll;
import com.v2soft.styxlib.utils.Polls;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientsRepoImpl implements ClientsRepo {
    private static final String TAG = "ClientsRepoImpl";
    public record Configuration(
            boolean noAuthenticationRequired
    ){}
    private final ConcurrentHashMap<Integer, ClientDetails> mClients;
    private final AtomicInteger mIdCounter = new AtomicInteger(1);
    private final Configuration mConfiguration;

    public ClientsRepoImpl(Configuration configuration) {
        mClients = new ConcurrentHashMap<>();
        mConfiguration = configuration;
    }

    @Override
    public int addClient(ClientDetails client) {
        int id = mIdCounter.getAndIncrement();
        mClients.put(id, client);
        client.setId(id);
        if (mConfiguration.noAuthenticationRequired) {
            client.setAuthenticated();
        }
        return id;
    }

    @Override
    public void removeClient(int id) {
        mClients.remove(id);
    }

    @Override
    public ClientDetails getClient(int id) throws StyxUnknownClientIdException {
        if (!mClients.containsKey(id)) {
            throw new StyxUnknownClientIdException("No client with id " + id + " found " + mClients);
        }
        return mClients.get(id);
    }

    @Override
    public IVirtualStyxFile getAssignedFile(int clientId, long fid) throws StyxErrorMessageException,
            StyxUnknownClientIdException {
        return getClient(clientId).getAssignedFile(fid);
    }

    @Override
    public IMessageTransmitter getDriver(int clientId) throws StyxUnknownClientIdException {
        return getClient(clientId).getDriver();
    }

    @Override
    public void closeFile(int clientId, long fid) throws StyxErrorMessageException, StyxUnknownClientIdException {
        var client = getClient(clientId);
        client.getAssignedFile(fid).close(clientId);
        client.closeFile(fid);
    }

    @Override
    public FIDPoll getFidPoll(int clientId) throws StyxUnknownClientIdException {
        return getClient(clientId).getPolls().getFIDPoll();
    }

    @Override
    public Polls getPolls(int clientId) throws StyxUnknownClientIdException {
        return getClient(clientId).getPolls();
    }

    @Override
    public IChannelDriver<?> getChannelDriver(int clientId) throws StyxUnknownClientIdException {
        return getClient(clientId).getDriver();
    }
}
