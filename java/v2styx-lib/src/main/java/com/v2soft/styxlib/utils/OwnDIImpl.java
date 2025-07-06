package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.StyxDeserializerImpl;
import com.v2soft.styxlib.l5.serialization.impl.StyxSerializerImpl;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCase;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCaseImpl;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.ClientsRepoImpl;

public class OwnDIImpl implements OwnDI {
    private ClientsRepo mClientsRepo = new ClientsRepoImpl();
    private IDataSerializer serializer = new StyxSerializerImpl();
    private IDataDeserializer deserializer = new StyxDeserializerImpl();
    private IsClientAuthorizedUseCaseImpl isClientAuthorizedUseCase =
            new IsClientAuthorizedUseCaseImpl(mClientsRepo);

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
}
