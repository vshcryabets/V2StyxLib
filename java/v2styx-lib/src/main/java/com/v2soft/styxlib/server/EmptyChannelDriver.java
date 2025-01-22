package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.utils.Future;

import java.util.Collection;
import java.util.List;

public class EmptyChannelDriver implements IChannelDriver {
    @Override
    public Thread start(int iounit) {
        return null;
    }

    @Override
    public void setTMessageHandler(IMessageProcessor handler) {

    }

    @Override
    public void setRMessageHandler(IMessageProcessor handler) {

    }

    @Override
    public Collection<ClientDetails> getClients() {
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
    public IDataSerializer getSerializer() {
        return null;
    }

    @Override
    public IDataDeserializer getDeserializer() {
        return null;
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
                                                         ClientDetails recipient,
                                                         long timeout) throws StyxException {
        throw new StyxException("Not implemented");
    }

    @Override
    public void close() {

    }
}
