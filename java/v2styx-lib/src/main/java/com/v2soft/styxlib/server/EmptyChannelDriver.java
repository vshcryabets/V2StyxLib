package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.utils.Future;

import java.util.Collection;
import java.util.List;

public class EmptyChannelDriver implements IChannelDriver {
    @Override
    public void prepare(InitConfiguration configuration) {

    }

    @Override
    public Thread start(StartConfiguration configuration) {
        return null;
    }

    @Override
    public Collection<Integer> getClients() {
        return List.of();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public int getTransmittedCount() {
        return 0;
    }

    @Override
    public int getErrorsCount() {
        return 0;
    }

    @Override
    public void clearStatistics() {

    }

    @Override
    public <R extends StyxMessage> Future<R> sendMessage(StyxMessage answer,
                                                         int recipient,
                                                         long timeout) throws StyxException {
        throw new StyxException("Not implemented");
    }

    @Override
    public void close() {

    }
}
