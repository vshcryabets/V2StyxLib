package com.v2soft.styxlib.server;

public interface IClientsRepo {
    int addClient();
    ClientDetails getClient(int id);
    void removeClient(int id);
}
