package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.Factory;
import com.v2soft.styxlib.l5.structs.StyxQID;

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

    @Test
    public void testCreateRVersion() {
        var message = factory.constructRVersion(16384, "9P2000");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(16384, ((StyxRVersionMessage) message).maxPacketSize);
        Assertions.assertEquals("9P2000", ((StyxRVersionMessage) message).protocolVersion);
    }

    @Test
    public void testCreateRAttachMessage() {
        var message = factory.constructRAttachMessage(1, StyxQID.EMPTY);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertTrue(message instanceof StyxRAttachMessage);
        Assertions.assertEquals(StyxQID.EMPTY, ((StyxRAttachMessage) message).mQID);
    }

    @Test
    public void testCreateRAuthMessage() {
        var message = factory.constructRAuthMessage(1, StyxQID.EMPTY);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertTrue(message instanceof StyxRAuthMessage);
        Assertions.assertEquals(StyxQID.EMPTY, ((StyxRAuthMessage) message).mQID);
    }

    @Test
    public void testCreateRCreateMessage() {
        var message = factory.constructRCreateMessage(1, StyxQID.EMPTY, 3642);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertTrue(message instanceof StyxROpenMessage);
        Assertions.assertEquals(MessageType.Rcreate, message.getType());
        Assertions.assertEquals(StyxQID.EMPTY, ((StyxROpenMessage) message).mQID);
        Assertions.assertEquals(3642, ((StyxROpenMessage) message).ioUnit);
    }

    @Test
    public void testCreateROpenMessage() {
        var message = factory.constructROpenMessage(1, StyxQID.EMPTY, 3642);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertTrue(message instanceof StyxROpenMessage);
        Assertions.assertEquals(MessageType.Ropen, message.getType());
        Assertions.assertEquals(StyxQID.EMPTY, ((StyxROpenMessage) message).mQID);
        Assertions.assertEquals(3642, ((StyxROpenMessage) message).ioUnit);
    }
}
