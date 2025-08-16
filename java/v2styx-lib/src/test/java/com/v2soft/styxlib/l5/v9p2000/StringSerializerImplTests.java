package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.messages.v9p2000.MessageFactoryImpl;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class StringSerializerImplTests {
    StringSerializerImpl serializer = new StringSerializerImpl();
    MessagesFactory messageFactory = new MessageFactoryImpl();

    @Test
    public void testSerializeQid() throws StyxException {
        Assertions.assertEquals("QID {type: 0, version: 0, path: 0}",
                serializer.serializeQid(StyxQID.EMPTY));
    }

    @Test
    public void testSerializeStat() throws StyxException {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.set(2001, 05, 04, 12, 30, 23);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        var date = utcCalendar.getTime();
        StyxStat stat = new StyxStat(
                0x10,
                0x20,
                StyxQID.EMPTY,
                0x123,
                date, date,
                123,
                "testName",
                "testUser",
                "testGroup",
                "testModUser"
        );

        Assertions.assertEquals("Stat 0x10,0x20,Qid=QID {type: 0, version: 0, path: 0},mode=0x123," +
                        "atime=2001-06-04T12:30:23Z,mtime=2001-06-04T12:30:23Z," +
                        "length=123,name=testName,user=testUser,group=testGroup,modUser=testModUser",
                serializer.serializeStat(stat));
    }

    @Test
    public void testSerializeRVersion() {
        var message = messageFactory.constructRVersion(1080, "9P2000.L");
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:101"));
        Assertions.assertTrue(str.contains("MaxPacketSize:1080"));
        Assertions.assertTrue(str.contains("ProtocolVersion:9P2000.L"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeTVersion() {
        var message = messageFactory.constructTVersion(1080, "9P2000.L");
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:100"));
        Assertions.assertTrue(str.contains("MaxPacketSize:1080"));
        Assertions.assertTrue(str.contains("ProtocolVersion:9P2000.L"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeRAttach() {
        var message = messageFactory.constructRAttachMessage(1080, StyxQID.EMPTY);
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:105"));
        Assertions.assertTrue(str.contains("QID {type: 0, version: 0, path: 0}"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeRAuth() {
        var message = messageFactory.constructRAuthMessage(1080, StyxQID.EMPTY);
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:103"));
        Assertions.assertTrue(str.contains("QID {type: 0, version: 0, path: 0}"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeRCreate() {
        var message = messageFactory.constructRCreateMessage(1080, StyxQID.EMPTY, 46243);
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:115"));
        Assertions.assertTrue(str.contains("iounit:46243"));
        Assertions.assertTrue(str.contains("QID {type: 0, version: 0, path: 0}"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeROpen() {
        var message = messageFactory.constructROpenMessage(1080, StyxQID.EMPTY, 46243);
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:113"));
        Assertions.assertTrue(str.contains("iounit:46243"));
        Assertions.assertTrue(str.contains("QID {type: 0, version: 0, path: 0}"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeTWrite() {
        var message = messageFactory.constructTWriteMessage(1080, 23432,
                new byte[]{0x01, 0x02, 0x03}, 0, 3);
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:118"));
        Assertions.assertTrue(str.contains("fileOffset:23432"));
        Assertions.assertTrue(str.contains("dataLength:3"));
        Assertions.assertTrue(str.contains("Tag:123"));
    }

    @Test
    public void testSerializeTWalk() {
        var message = messageFactory.constructTWalkMessage(1080, 23432,
                List.of("path1", "path2", "path3"));
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:110"));
        Assertions.assertTrue(str.contains("fid:1080"));
        Assertions.assertTrue(str.contains("newFid:23432"));
        Assertions.assertTrue(str.contains("pathElements:[path1, path2, path3]"));
    }

    @Test
    public void testSerializeTWStat() {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.set(2001, 05, 04, 12, 30, 23);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        var date = utcCalendar.getTime();
        StyxStat stat = new StyxStat(
                0x10,
                0x20,
                StyxQID.EMPTY,
                0x123,
                date, date,
                123,
                "testName",
                "testUser",
                "testGroup",
                "testModUser"
        );
        var message = messageFactory.constructTWStatMessage(1080, stat);
        message.setTag(123);
        var str = serializer.serializeMessage(message);
        Assertions.assertTrue(str.contains("Message Type:126"));
        Assertions.assertTrue(str.contains("fid:1080"));
        Assertions.assertTrue(str.contains("Stat 0x10,0x20,Qid=QID {type: 0, version: 0, path: 0},mode=0x123," +
                        "atime=2001-06-04T12:30:23Z,mtime=2001-06-04T12:30:23Z," +
                        "length=123,name=testName,user=testUser,group=testGroup,modUser=testModUser"));
    }
}
