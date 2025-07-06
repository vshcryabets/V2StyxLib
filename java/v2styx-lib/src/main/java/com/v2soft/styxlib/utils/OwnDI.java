package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCase;
import com.v2soft.styxlib.server.ClientsRepo;

public interface OwnDI {
    IsClientAuthorizedUseCase getIsClientAuthorizedUseCase();
    IDataDeserializer getDataDeserializer();
    IDataSerializer getDataSerializer();
    ClientsRepo getClientsRepo();
}
