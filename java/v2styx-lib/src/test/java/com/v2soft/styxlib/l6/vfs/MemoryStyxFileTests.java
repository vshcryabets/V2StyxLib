package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxNotAuthorizedException;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.utils.OwnDI;
import com.v2soft.styxlib.utils.OwnDIImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

public class MemoryStyxFileTests {
    private OwnDI di = new OwnDIImpl();

    @Test
    public void testMemoryStyxFileCredentialFails() throws StyxException {
        MemoryStyxFile file = new MemoryStyxFile("testfile", di);
        Assertions.assertThrows(StyxNotAuthorizedException.class, () -> file.open(1, ModeType.OREAD));
        Assertions.assertThrows(StyxNotAuthorizedException.class, () ->
                file.walk(new LinkedList<>(), Collections.emptyList()));
        byte [] data = new byte[10];
        Assertions.assertThrows(StyxNotAuthorizedException.class, () -> file.read(1, data, 0, 10));
    }
}
