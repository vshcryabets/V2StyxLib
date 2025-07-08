package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.utils.Polls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientsRepoImplTest {
    private ClientsRepoImpl clientsRepo;
    private ClientDetails mockClient;
    private IChannelDriver mockChannelDriver;

    @BeforeEach
    void setUp() {
        clientsRepo = new ClientsRepoImpl(new ClientsRepoImpl.Configuration(false));
        mockChannelDriver = new EmptyChannelDriver();
        mockClient = new ClientDetails(mockChannelDriver);
    }

    @Test
    void addClientShouldAssignUniqueId() {
        int clientId1 = clientsRepo.addClient(mockClient);
        int clientId2 = clientsRepo.addClient(mockClient);
        assertNotEquals(clientId1, clientId2);
    }

    @Test
    void removeClientShouldRemoveClientById() throws StyxUnknownClientIdException {
        int clientId = clientsRepo.addClient(mockClient);
        clientsRepo.removeClient(clientId);
        assertThrows(StyxUnknownClientIdException.class, () -> clientsRepo.getClient(clientId));
    }

    @Test
    void getClientShouldReturnCorrectClient() throws StyxUnknownClientIdException {
        int clientId = clientsRepo.addClient(mockClient);
        assertEquals(mockClient, clientsRepo.getClient(clientId));
    }

    @Test
    void getUnknownClientShouldThrowException() {
        assertThrows(StyxUnknownClientIdException.class, () -> clientsRepo.getClient(9999));
    }

    @Test
    void getAssignedFileShouldThrowExceptionForInvalidFid() throws StyxErrorMessageException {
        int clientId = clientsRepo.addClient(mockClient);
        assertThrows(StyxErrorMessageException.class, () -> clientsRepo.getAssignedFile(clientId, 123L));
    }

    @Test
    void getFidPollShouldReturnCorrectPoll() throws StyxUnknownClientIdException {
        int clientId = clientsRepo.addClient(mockClient);
        assertEquals(mockClient.getPolls().getFIDPoll(), clientsRepo.getFidPoll(clientId));
    }

    @Test
    void getPollsShouldReturnCorrectPolls() throws StyxUnknownClientIdException {
        int clientId = clientsRepo.addClient(mockClient);
        Polls mockPolls = mockClient.getPolls();
        assertEquals(mockPolls, clientsRepo.getPolls(clientId));
    }

    @Test
    void getChannelDriverShouldReturnCorrectDriver() throws StyxUnknownClientIdException {
        int clientId = clientsRepo.addClient(mockClient);
        assertEquals(mockChannelDriver, clientsRepo.getChannelDriver(clientId));
    }

    @Test
    void clientShouldBeAuthorizedWhenAuthorizationDisabled() throws StyxUnknownClientIdException {
        ClientsRepoImpl repo = new ClientsRepoImpl(new ClientsRepoImpl.Configuration(true));
        int clientId = repo.addClient(mockClient);
        assertTrue(repo.getClient(clientId).isAuthenticated());
    }
}