package com.v2soft.styxlib.l4;

public interface ClientsRepo {
    ClientId getNextClientId();
    void releaseClientId(ClientId id);
}
