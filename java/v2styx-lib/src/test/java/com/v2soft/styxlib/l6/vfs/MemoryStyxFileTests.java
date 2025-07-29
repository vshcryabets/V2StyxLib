package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxNotAuthorizedException;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.EmptyChannelDriver;
import com.v2soft.styxlib.utils.StyxSessionDI;
import com.v2soft.styxlib.utils.impl.StyxSessionDIImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

public class MemoryStyxFileTests {

    @Test
    public void testMemoryStyxFileCredentialFails() throws StyxException {
        StyxSessionDI di = new StyxSessionDIImpl(true);
        MemoryStyxFile file = new MemoryStyxFile("testfile", di);
        var clientId = di.getClientsRepo().addClient(new ClientDetails(new EmptyChannelDriver()));
        Assertions.assertThrows(StyxNotAuthorizedException.class, () -> file.open(clientId, ModeType.OREAD));
        Assertions.assertThrows(StyxNotAuthorizedException.class, () ->
                file.walk(clientId, new LinkedList<>(), Collections.emptyList()));
        byte [] data = new byte[10];
        Assertions.assertThrows(StyxNotAuthorizedException.class, () -> file.read(clientId, data, 0, 10));
    }
}
