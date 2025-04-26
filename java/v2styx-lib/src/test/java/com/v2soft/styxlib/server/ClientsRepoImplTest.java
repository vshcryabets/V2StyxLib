package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
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
        clientsRepo = new ClientsRepoImpl();
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
    void removeClientShouldRemoveClientById() {
        int clientId = clientsRepo.addClient(mockClient);
        clientsRepo.removeClient(clientId);
        assertNull(clientsRepo.getClient(clientId));
    }

    @Test
    void getClientShouldReturnCorrectClient() {
        int clientId = clientsRepo.addClient(mockClient);

        assertEquals(mockClient, clientsRepo.getClient(clientId));
    }

    @Test
    void getAssignedFileShouldThrowExceptionForInvalidFid() throws StyxErrorMessageException {
        int clientId = clientsRepo.addClient(mockClient);
        assertThrows(StyxErrorMessageException.class, () -> clientsRepo.getAssignedFile(clientId, 123L));
    }

    @Test
    void getFidPollShouldReturnCorrectPoll() {
        int clientId = clientsRepo.addClient(mockClient);
        assertEquals(mockClient.getPolls().getFIDPoll(), clientsRepo.getFidPoll(clientId));
    }

    @Test
    void getPollsShouldReturnCorrectPolls() {
        int clientId = clientsRepo.addClient(mockClient);
        Polls mockPolls = mockClient.getPolls();
        assertEquals(mockPolls, clientsRepo.getPolls(clientId));
    }

    @Test
    void getChannelDriverShouldReturnCorrectDriver() {
        int clientId = clientsRepo.addClient(mockClient);
        assertEquals(mockChannelDriver, clientsRepo.getChannelDriver(clientId));
    }
}