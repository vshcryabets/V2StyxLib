package com.v2soft.styxlib.l6;

import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;

public interface IsClientAuthorizedUseCase {
    boolean isClientAuthorized(int clientId) throws StyxUnknownClientIdException;
}
