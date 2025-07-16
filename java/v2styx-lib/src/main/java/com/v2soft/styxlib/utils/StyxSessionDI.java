package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.l5.dev.StringSerializer;
import com.v2soft.styxlib.l5.messages.base.Factory;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCase;
import com.v2soft.styxlib.server.ClientsRepo;

public interface StyxSessionDI {
    IsClientAuthorizedUseCase getIsClientAuthorizedUseCase();

    IDataDeserializer getDataDeserializer();
    IDataSerializer getDataSerializer();
    StringSerializer getStringSerializer();
    Factory getMessageFactory();

    ClientsRepo getClientsRepo();
}
