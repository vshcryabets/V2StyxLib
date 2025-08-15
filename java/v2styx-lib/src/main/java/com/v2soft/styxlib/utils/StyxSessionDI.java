package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.l5.dev.StringSerializer;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l6.IsClientAuthorizedUseCase;
import com.v2soft.styxlib.server.ClientsRepo;

public interface StyxSessionDI {
    IsClientAuthorizedUseCase getIsClientAuthorizedUseCase();

    // processors
    IDataDeserializer getDataDeserializer();
    IDataSerializer getDataSerializer();
    StringSerializer getStringSerializer();
    MessagesFactory getMessageFactory();

    // repos
    ClientsRepo getClientsRepo();
    CompletablesMap getCompletablesMap(int clientId) throws StyxUnknownClientIdException;

    // uses cases
    GetMessagesFactoryUseCase getGetMessagesFactoryUseCase();
}
