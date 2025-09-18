package com.v2soft.styxlib.l4.impl;

import com.v2soft.styxlib.l4.ClientId;
import com.v2soft.styxlib.l4.ClientsRepo;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientsRepoImpl implements ClientsRepo {
    private AtomicInteger nextId = new AtomicInteger(0);

    @Override
    public ClientId getNextClientId() {
        return new ClientId(nextId.getAndIncrement());
    }

    @Override
    public void releaseClientId(ClientId id) {
        // No-op for now
    }

    public ClientId getLastIssuedId() {
        return new ClientId(nextId.get() - 1);
    }
}
