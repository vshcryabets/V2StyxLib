package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.l5.dev.StringSerializer;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.v9p2000.StyxDeserializerImpl;
import com.v2soft.styxlib.l5.v9p2000.StyxSerializerImpl;
import com.v2soft.styxlib.l5.v9p2000.StringSerializerImpl;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCase;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCaseImpl;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.ClientsRepoImpl;

public class StyxSessionDIImpl implements StyxSessionDI {
    private final ClientsRepo mClientsRepo;
    private final IDataSerializer serializer = new StyxSerializerImpl();
    private final IDataDeserializer deserializer = new StyxDeserializerImpl();
    private final IsClientAuthorizedUseCaseImpl isClientAuthorizedUseCase;
    private final StringSerializer stringSerializer;

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
    public StringSerializer getStringSerializer() {
        return stringSerializer;
    }
}
