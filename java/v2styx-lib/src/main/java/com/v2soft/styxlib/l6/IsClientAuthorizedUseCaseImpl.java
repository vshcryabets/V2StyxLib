package com.v2soft.styxlib.l6;

import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.server.ClientsRepo;

public class IsClientAuthorizedUseCaseImpl implements IsClientAuthorizedUseCase {

    private final ClientsRepo mClientsRepo;

    public IsClientAuthorizedUseCaseImpl(ClientsRepo clientsRepo) {
        this.mClientsRepo = clientsRepo;
    }

    @Override
    public boolean isClientAuthorized(int clientId) throws StyxUnknownClientIdException {
        return mClientsRepo.getClient(clientId).isAuthenticated();
    }
}
