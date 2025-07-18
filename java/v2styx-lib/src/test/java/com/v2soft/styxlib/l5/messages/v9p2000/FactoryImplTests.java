package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.messages.base.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FactoryImplTests {
    final Factory factory = new FactoryImpl();

    @Test
    public void testCreateTVersion() {
        var message = factory.constructTVersion(16384,   "9P2000");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(16384, ((StyxTVersionMessage) message).maxPacketSize);
        Assertions.assertEquals("9P2000", ((StyxTVersionMessage) message).protocolVersion);
    }

    @Test
    public void testCreateTAuth() {
        var message = factory.constructTAuth(0x20, "user", "test");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x20, ((StyxTAuthMessage) message).getFID());
        Assertions.assertEquals("user", ((StyxTAuthMessage) message).mUserName);
        Assertions.assertEquals("test", ((StyxTAuthMessage) message).mMountPoint);
    }

    @Test
    public void testCreateTAttach() {
        var message = factory.constructTAttach(1, 2, "user", "test");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, ((StyxTAttachMessage) message).getFID());
        Assertions.assertEquals(2, ((StyxTAttachMessage) message).authFID);
        Assertions.assertEquals("user", ((StyxTAttachMessage) message).userName);
        Assertions.assertEquals("test", ((StyxTAttachMessage) message).mountPoint);
    }

    @Test
    public void testCreateRerror() {
        var message = factory.constructRerror(1, "Test error");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertTrue(message instanceof StyxRErrorMessage);
        Assertions.assertEquals("Test error", ((StyxRErrorMessage) message).mError);
    }
}
