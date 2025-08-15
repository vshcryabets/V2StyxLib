package com.v2soft.styxlib.utils.impl;

import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.l5.dev.StringSerializer;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.v9p2000.StyxDeserializerImpl;
import com.v2soft.styxlib.l5.v9p2000.StyxSerializerImpl;
import com.v2soft.styxlib.l5.v9p2000.StringSerializerImpl;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCase;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCaseImpl;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.ClientsRepoImpl;
import com.v2soft.styxlib.utils.CompletablesMap;
import com.v2soft.styxlib.utils.GetMessagesFactoryUseCase;
import com.v2soft.styxlib.utils.StyxSessionDI;

public class StyxSessionDIImpl implements StyxSessionDI {
    private final ClientsRepo mClientsRepo;
    private final IDataSerializer serializer = new StyxSerializerImpl();
    private final IsClientAuthorizedUseCaseImpl isClientAuthorizedUseCase;
    private final StringSerializer stringSerializer;
    private final MessagesFactory messageFactory = new com.v2soft.styxlib.l5.messages.v9p2000.FactoryImpl();
    private final IDataDeserializer deserializer = new StyxDeserializerImpl(messageFactory);

    public StyxSessionDIImpl(boolean authRequired) {
        mClientsRepo = new ClientsRepoImpl(new ClientsRepoImpl.Configuration(!authRequired));
        isClientAuthorizedUseCase = new IsClientAuthorizedUseCaseImpl(mClientsRepo);
        stringSerializer = new StringSerializerImpl();
    }

    @Override
    public IsClientAuthorizedUseCase getIsClientAuthorizedUseCase() {
        return isClientAuthorizedUseCase;
    }

    @Override
    public IDataDeserializer getDataDeserializer() {
        return deserializer;
    }

    @Override
    public IDataSerializer getDataSerializer() {
        return serializer;
    }

    @Override
    public ClientsRepo getClientsRepo() {
        return mClientsRepo;
    }

    @Override
    public CompletablesMap getCompletablesMap(int clientId) throws StyxUnknownClientIdException {
        return mClientsRepo.getPolls(clientId);
    }

    @Override
    public GetMessagesFactoryUseCase getGetMessagesFactoryUseCase() {
        return () -> messageFactory;
    }

    @Override
    public StringSerializer getStringSerializer() {
        return stringSerializer;
    }

    @Override
    public MessagesFactory getMessageFactory() {
        return messageFactory;
    }
}
