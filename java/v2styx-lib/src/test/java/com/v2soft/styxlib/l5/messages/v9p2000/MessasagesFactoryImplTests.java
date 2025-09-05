package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MessasagesFactoryImplTests {
    final MessagesFactory factory = new MessageFactoryImpl();

    @Test
    public void testCreateTVersion() {
        var message = factory.constructTVersion(16384,   "9P2000");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(16384, ((BaseMessage) message).getIounit());
        Assertions.assertEquals("9P2000", ((StyxTVersionMessage) message).protocolVersion);
    }

    @Test
    public void testCreateTAuth() {
        var message = factory.constructTAuth(0x20, "user", "test");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x20, ((BaseMessage) message).getFID());
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
        Assertions.assertInstanceOf(StyxRErrorMessage.class, message);
        Assertions.assertEquals("Test error", ((StyxRErrorMessage) message).mError);
    }

    @Test
    public void testCreateRVersion() {
        var message = factory.constructRVersion(16384, "9P2000");
        Assertions.assertNotNull(message);
        Assertions.assertEquals(16384, ((BaseMessage) message).getIounit());
        Assertions.assertEquals("9P2000", ((BaseMessage) message).getProtocolVersion());
    }

    @Test
    public void testCreateRAttachMessage() {
        var message = factory.constructRAttachMessage(1, QID.EMPTY);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertInstanceOf(BaseMessage.class, message);
        Assertions.assertEquals(QID.EMPTY, ((BaseMessage) message).qid);
    }

    @Test
    public void testCreateRAuthMessage() {
        var message = factory.constructRAuthMessage(1, QID.EMPTY);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertInstanceOf(BaseMessage.class, message);
        Assertions.assertEquals(QID.EMPTY, ((BaseMessage) message).qid);
    }

    @Test
    public void testCreateRCreateMessage() {
        var message = factory.constructRCreateMessage(1, QID.EMPTY, 3642);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertInstanceOf(BaseMessage.class, message);
        Assertions.assertEquals(MessageType.Rcreate, message.getType());
        Assertions.assertEquals(QID.EMPTY, ((BaseMessage) message).qid);
        Assertions.assertEquals(3642, ((BaseMessage) message).getIounit());
    }

    @Test
    public void testCreateROpenMessage() {
        var message = factory.constructROpenMessage(1, QID.EMPTY, 3642);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertInstanceOf(BaseMessage.class, message);
        Assertions.assertEquals(MessageType.Ropen, message.getType());
        Assertions.assertEquals(QID.EMPTY, ((BaseMessage) message).qid);
        Assertions.assertEquals(3642, ((BaseMessage) message).getIounit());
    }

    @Test
    public void testCreateTWrite() {
        var data = new byte[]{0x01, 0x02, 0x03};
        var message = factory.constructTWriteMessage(1,0x123,
                data,
                0,
                3);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(Constants.NOTAG, message.getTag());
        Assertions.assertInstanceOf(StyxTWriteMessage.class, message);
        Assertions.assertEquals(MessageType.Twrite, message.getType());
        var qid =  ((StyxTWriteMessage) message).qid;
        Assertions.assertTrue(qid == null || qid == QID.EMPTY);
        Assertions.assertEquals(0x123, ((StyxTWriteMessage) message).offset);
        Assertions.assertArrayEquals(data, ((StyxTWriteMessage) message).data);
    }

    @Test
    public void testCreateTWalk() {
        var message = factory.constructTWalkMessage(1080, 23432,
                List.of("path1", "path2", "path3"));
        Assertions.assertNotNull(message);
        Assertions.assertEquals(Constants.NOTAG, message.getTag());
        Assertions.assertInstanceOf(StyxTWalkMessage.class, message);
        Assertions.assertEquals(MessageType.Twalk, message.getType());
        Assertions.assertEquals(1080, ((StyxTWalkMessage) message).getFID());
        Assertions.assertEquals(23432, ((StyxTWalkMessage) message).mNewFID);
        Assertions.assertEquals(3, ((StyxTWalkMessage) message).mPathElements.size());
    }

    @Test
    public void testCreateTWStat() {
        var message = factory.constructTWStatMessage(1080, StyxStat.EMPTY);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(Constants.NOTAG, message.getTag());
        Assertions.assertInstanceOf(StyxTWStatMessage.class, message);
        Assertions.assertEquals(MessageType.Twstat, message.getType());
        Assertions.assertEquals(1080, ((StyxTWStatMessage) message).getFID());
        Assertions.assertEquals(StyxStat.EMPTY, ((StyxTWStatMessage) message).stat);
    }

    @Test
    public void testCreateRStat() {
        var message = factory.constructRStatMessage(1, StyxStat.EMPTY);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertInstanceOf(StyxRStatMessage.class, message);
        Assertions.assertEquals(MessageType.Rstat, message.getType());
        Assertions.assertEquals(StyxStat.EMPTY, ((StyxRStatMessage) message).stat);
    }

    @Test
    public void testCreateTFlush() {
        var message = factory.constructTFlushMessage(1);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, ((StyxTFlushMessage)message).oldTag);
        Assertions.assertInstanceOf(StyxTFlushMessage.class, message);
        Assertions.assertEquals(MessageType.Tflush, message.getType());
    }

    @Test
    public void testCreateTOpen() {
        var message = factory.constructTOpenMessage(1, (int) FileMode.AppendOnly);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, ((StyxTOpenMessage)message).getFID());
        Assertions.assertEquals(FileMode.AppendOnly, ((StyxTOpenMessage)message).mode);
        Assertions.assertInstanceOf(StyxTOpenMessage.class, message);
        Assertions.assertEquals(MessageType.Topen, message.getType());
    }

    @Test
    public void testCreateRWrite() {
        var message = factory.constructRWriteMessage(1, 12345);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertEquals(12345, ((StyxRWriteMessage)message).count);
        Assertions.assertInstanceOf(StyxRWriteMessage.class, message);
        Assertions.assertEquals(MessageType.Rwrite, message.getType());
    }

    @Test
    public void testCreateTRead() {
        var message = factory.constructTReadMessage(1, 12345, 67890);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, ((StyxTReadMessage)message).getFID());
        Assertions.assertEquals(12345, ((StyxTReadMessage)message).offset);
        Assertions.assertEquals(67890, ((StyxTReadMessage)message).count);
        Assertions.assertInstanceOf(StyxTReadMessage.class, message);
        Assertions.assertEquals(MessageType.Tread, message.getType());
    }

    @Test
    public void testCreateRWalk() {
        var message = factory.constructRWalkMessage(1,
                List.of(QID.EMPTY, QID.EMPTY)
        );
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertEquals(2, ((StyxRWalkMessage)message).qidList.size());
        Assertions.assertInstanceOf(StyxRWalkMessage.class, message);
        Assertions.assertEquals(MessageType.Rwalk, message.getType());
    }

    @Test
    public void testCreateRRead() {
        var data = new byte[]{0x01, 0x02, 0x03};
        var message = factory.constructRReadMessage(1,
                data,
                3
        );
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, message.getTag());
        Assertions.assertEquals(3, ((StyxRReadMessage)message).dataLength);
        Assertions.assertArrayEquals(data, ((StyxRReadMessage)message).data);
        Assertions.assertInstanceOf(StyxRReadMessage.class, message);
        Assertions.assertEquals(MessageType.Rread, message.getType());
    }

    @Test
    public void testCreateTCreate() {
        var message = factory.constructTCreateMessage(1,
                "testFile",
                0x123,
                ModeType.ORDWR
        );
        Assertions.assertNotNull(message);
        Assertions.assertEquals(1, ((StyxTCreateMessage)message).getFID());
        Assertions.assertEquals("testFile", ((StyxTCreateMessage)message).name);
        Assertions.assertEquals(0x123, ((StyxTCreateMessage)message).permissions);
        Assertions.assertEquals(ModeType.ORDWR, ((StyxTCreateMessage)message).mode);
        Assertions.assertInstanceOf(StyxTCreateMessage.class, message);
        Assertions.assertEquals(MessageType.Tcreate, message.getType());
    }

    @Test
    public void testCreateTClunk() {
        var message = factory.constructTClunk(0x123);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, ((BaseMessage)message).getFID());
        Assertions.assertEquals(MessageType.Tclunk, message.getType());
    }

    @Test
    public void testCreateRClunk() {
        var message = factory.constructRClunk(0x123, 0x2345FF80);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, message.getTag());
        Assertions.assertEquals(0x2345FF80, ((BaseMessage)message).getFID());
        Assertions.assertEquals(MessageType.Rclunk, message.getType());
    }

    @Test
    public void testCreateTRemove() {
        var message = factory.constructTRemove(0x123);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, ((BaseMessage)message).getFID());
        Assertions.assertEquals(MessageType.Tremove, message.getType());
    }

    @Test
    public void testCreateRRemove() {
        var message = factory.constructRRemove(0x123);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, message.getTag());
        Assertions.assertEquals(MessageType.Rremove, message.getType());
    }

    @Test
    public void testCreateTStat() {
        var message = factory.constructTStat(0x123);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, ((BaseMessage)message).getFID());
        Assertions.assertEquals(MessageType.Tstat, message.getType());
    }

    @Test
    public void testCreateRWStat() {
        var message = factory.constructRWStat(0x123);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, message.getTag());
        Assertions.assertEquals(MessageType.Rwstat, message.getType());
    }

    @Test
    public void testCreateRFlush() {
        var message = factory.constructRFlush(0x123);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(0x123, message.getTag());
        Assertions.assertEquals(MessageType.Rflush, message.getType());
    }
}
