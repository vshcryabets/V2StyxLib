package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.messages.base.MessagesFactory;
import com.v2soft.styxlib.l5.messages.v9p2000.MessageFactoryImpl;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.BufferWriterImpl;
import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StyxSerializerImplTest {
    MessagesFactory messageFactory = new MessageFactoryImpl();
    StyxSerializerImpl serializer = new StyxSerializerImpl();

    @Test
    void testGetSize() {
        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2 + 2,
                serializer.getMessageSize(messageFactory.constructRerror(0, "AB")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(messageFactory.constructTStat(0x2345)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(messageFactory.constructTClunk(0x2345)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(messageFactory.constructRClunk(0x2345, 0x0CAFE)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(messageFactory.constructTRemove(0x2345)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 2 + 6 + 12,
                serializer.getMessageSize(messageFactory.constructTAttach(
                        0x2345,
                        0x3333,
                        "user",
                        "mountpoint")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize(),
                serializer.getMessageSize(messageFactory.constructRAttachMessage(
                        0x10,
                        new QID(QidType.QTFILE, 1, 2))));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 6 + 12,
                serializer.getMessageSize(messageFactory.constructTAuth(
                        0x1234,
                        "user",
                        "mountpoint")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 6 + 3,
                serializer.getMessageSize(messageFactory.constructTAuth(
                        1,
                        "user",
                        "/")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 4 + 2 + 3 + 3 + 3,
                serializer.getMessageSize(messageFactory.constructTWalkMessage(
                        0x1234,
                        0x9876,
                        List.of("a","b","c"))));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 1,
                serializer.getMessageSize(messageFactory.constructTOpenMessage(
                        0x1234,
                        1)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 5 + 6,
                serializer.getMessageSize(messageFactory.constructTCreateMessage(
                        0x1234,
                        "name",
                        0x33,
                        0x22)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 68,
                serializer.getMessageSize(messageFactory.constructTWStatMessage(
                        0x1234,
                        new StyxStat(
                                (short)1,
                                2,
                                new QID(QidType.QTFILE, 0x80, 0x90),
                                0x01,
                                new Date(),
                                new Date(),
                                0x123,
                                "file",
                                "user",
                                "group",
                              "editor"
                        )
                        )));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 8 + 4 + 4,
                serializer.getMessageSize(messageFactory.constructTWriteMessage(0x1234, 0x3333,
                        new byte[]{0x1, 0x2, 0x3, 0x4},
                        0,
                        4
                        )));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 8 + 4,
                serializer.getMessageSize(messageFactory.constructTReadMessage(0x1111, 0x2222, 0x3333)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(messageFactory.constructRWriteMessage(0x1111, 0x2222)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 51,
                serializer.getMessageSize(messageFactory.constructRStatMessage(
                        0x1111, StyxStat.EMPTY)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 4,
                serializer.getMessageSize(messageFactory.constructRReadMessage(0x1111, new byte[]{0x1, 0x2, 0x3, 0x4}, 4)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2,
                serializer.getMessageSize(messageFactory.constructTFlushMessage(0x1111)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 4,
                serializer.getMessageSize(messageFactory.constructTVersion(0x1111, "ABCD")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize() + 4,
                serializer.getMessageSize(messageFactory.constructROpenMessage(0x1111, new QID(
                        QidType.QTFILE, 1, 2
                ), 0)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize() + 4,
                serializer.getMessageSize(messageFactory.constructRCreateMessage(0x1111, new QID(
                        QidType.QTFILE, 1, 2
                ), 0)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 4,
                serializer.getMessageSize(messageFactory.constructRVersion(1, 0x1111, "ABCD")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2 + serializer.getQidSize(),
                serializer.getMessageSize(messageFactory.constructRWalkMessage(0x1111, Collections.singletonList(new QID(
                        QidType.QTFILE, 1, 2
                )))));

    }

    @Test
    void testGetStyxStatSize() {
        // empty stat = 28 + 7 + 2 + 2 + 2 + 2
        assertEquals(28 + 13 + 2 + 2 + 2 + 2, serializer.getStatSerializedSize(StyxStat.EMPTY));
    }

    @Test
    void testGetQidSize() {
        assertEquals(13, serializer.getQidSize());
    }

    @Test
    void testQidSerialization() throws StyxException {
        var qid = new QID(
                QidType.QTDIR,
                0x6A7470F1,
                0x12309E51049E5104L
        );
        byte[] expected = {
                (byte) QidType.QTDIR,
                (byte) 0xF1, 0x70, 0x74, 0x6A, //9: qid.version[4] 0x6A7470F1
                0x04, 0x51, (byte) 0x9E, 0x04, 0x51, (byte) 0x9E, 0x30, 0x12 //13: qid.path[8] 0x12309E51049E5104L
        };

        var outputBuffer = new BufferWriterImpl(8192);
        var buffer = outputBuffer.getBuffer();
        outputBuffer.prepareBuffer(serializer.getQidSize());
        serializer.serializeQid(qid, outputBuffer);
        byte[] data = new byte[buffer.limit()];
        buffer.position(0);
        buffer.get(data);
        assertArrayEquals(expected, data);
    }

    @Test
    void testTClunkSerialization() throws StyxException {
        var message = messageFactory.constructTClunk(0x1234);
        BufferWriterImpl output = new BufferWriterImpl(8192);
        serializer.serialize(message, output);

        // Validate buffer size and some expected values
        var buffer = output.getBuffer();
        assertEquals(serializer.getMessageSize(message), output.getPosition());

        byte[] expected = {
                11, 0x00, 0x00, 0x00, // size
                (byte) MessageType.Tclunk, // type
                0x00, 0x00, // Tag
                0x34, 0x12, 0x00, 0x00 // fid
        };

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        assertArrayEquals(expected, data);
    }

    @Test
    void testRClunkSerialization() throws StyxException {
        var message = messageFactory.constructRClunk(0, 0x1234);
        BufferWriterImpl output = new BufferWriterImpl(8192);
        serializer.serialize(message, output);

        // Validate buffer size and some expected values
        var buffer = output.getBuffer();
        assertEquals(serializer.getMessageSize(message), output.getPosition());

        byte[] expected = {
                11, 0x00, 0x00, 0x00, // size
                (byte) MessageType.Rclunk, // type
                0x00, 0x00, // Tag
                0x34, 0x12, 0x00, 0x00 // fid
        };

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        assertArrayEquals(expected, data);
    }

    @Test
    void testTRemoveSerialization() throws StyxException {
        var message = messageFactory.constructTRemove(0x1234);
        BufferWriterImpl output = new BufferWriterImpl(8192);
        serializer.serialize(message, output);

        // Validate buffer size and some expected values
        var buffer = output.getBuffer();
        assertEquals(serializer.getMessageSize(message), output.getPosition());

        byte[] expected = {
                11, 0x00, 0x00, 0x00, // size
                (byte) MessageType.Tremove, // type
                0x00, 0x00, // Tag
                0x34, 0x12, 0x00, 0x00 // fid
        };

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        assertArrayEquals(expected, data);
    }

    @Test
    void testRRemoveSerialization() throws StyxException {
        var message = messageFactory.constructRRemove(0x1234);
        BufferWriterImpl output = new BufferWriterImpl(8192);
        serializer.serialize(message, output);

        // Validate buffer size and some expected values
        var buffer = output.getBuffer();
        assertEquals(serializer.getMessageSize(message), output.getPosition());

        byte[] expected = {
                7, 0x00, 0x00, 0x00, // size
                (byte) MessageType.Rremove, // type
                0x34, 0x12
        };

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        assertArrayEquals(expected, data);
    }

    @Test
    void testSerializeStat() throws StyxException {
        StyxStat stat = new StyxStat(
                (short) 1,
                2,
                new QID(QidType.QTFILE, 0x80, 0x90),
                0x01,
                new Date(1717171717L * 1000), // fixed date for reproducibility
                new Date(1717171717L * 1000),
                0x123,
                "file",
                "user",
                "group",
                "editor"
        );
        BufferWriterImpl output = new BufferWriterImpl(8192);
        serializer.serializeStat(stat, output);

        // Validate buffer size and some expected values
        var buffer = output.getBuffer();
        assertEquals(serializer.getStatSerializedSize(stat), output.getPosition());

        byte[] expected = {
                66, 0x00, // size - 2
                1, 0x00, // type
                0x02, 0x00, 0x00, 0x00, // dev
                (byte) QidType.QTFILE,
                (byte)0x80, 0x00, 0x00, 0x00, //9: qid.version[4]
                (byte)0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //13: qid.path[8] 0x12309E51049E5104L
                0x01, 0x00, 0x00, 0x00, // mode
                0x05, (byte)0xF6, 89, 102, // atime
                0x05, (byte)0xF6, 89, 102, // mtime
                0x23, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // length
                0x04, 0x00, // name length
                'f', 'i', 'l', 'e', // name
                0x04, 0x00, // uid length
                'u', 's', 'e', 'r', // uid
                0x05, 0x00, // gid length
                'g', 'r', 'o', 'u', 'p', // gid
                0x06, 0x00, // muid length
                'e', 'd', 'i', 't', 'o', 'r' // muid
        };

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
//        buffer.position(0);
        buffer.get(data);
        assertArrayEquals(expected, data);
    }
}