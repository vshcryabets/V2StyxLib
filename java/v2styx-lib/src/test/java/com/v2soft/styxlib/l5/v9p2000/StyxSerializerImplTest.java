package com.v2soft.styxlib.l5.v9p2000;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.messages.StyxROpenMessage;
import com.v2soft.styxlib.l5.messages.StyxRReadMessage;
import com.v2soft.styxlib.l5.messages.StyxRStatMessage;
import com.v2soft.styxlib.l5.messages.StyxRWalkMessage;
import com.v2soft.styxlib.l5.messages.StyxRWriteMessage;
import com.v2soft.styxlib.l5.messages.StyxTCreateMessage;
import com.v2soft.styxlib.l5.messages.StyxTFlushMessage;
import com.v2soft.styxlib.l5.messages.StyxTOpenMessage;
import com.v2soft.styxlib.l5.messages.StyxTReadMessage;
import com.v2soft.styxlib.l5.messages.StyxTWStatMessage;
import com.v2soft.styxlib.l5.messages.StyxTWalkMessage;
import com.v2soft.styxlib.l5.messages.StyxTWriteMessage;
import com.v2soft.styxlib.l5.messages.base.Factory;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.FactoryImpl;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.BufferWriterImpl;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class StyxSerializerImplTest {
    Factory messageFactory = new FactoryImpl();
    StyxSerializerImpl serializer = new StyxSerializerImpl();

    @Test
    void testGetSize() {
        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2 + 2,
                serializer.getMessageSize(messageFactory.constructRerror(0, "AB")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxTMessageFID(MessageType.Unspecified,
                        MessageType.Rread,
                        0x2345)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 2 + 6 + 12,
                serializer.getMessageSize(messageFactory.constructTAttach(
                        0x2345,
                        0x3333,
                        "user",
                        "mountpoint")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize(),
                serializer.getMessageSize(new BaseMessage(
                        MessageType.Unspecified,
                        0x10,
                        new StyxQID(QidType.QTFILE, 1, 2))));

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
                serializer.getMessageSize(new StyxTWalkMessage(
                        0x1234,
                        0x9876,
                        List.of("a","b","c"))));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 1,
                serializer.getMessageSize(new StyxTOpenMessage(
                        0x1234,
                        1)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 5 + 6,
                serializer.getMessageSize(new StyxTCreateMessage(
                        0x1234,
                        "name",
                        0x33,
                        0x22)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 68,
                serializer.getMessageSize(new StyxTWStatMessage(
                        0x1234,
                        new StyxStat(
                                (short)1,
                                2,
                                new StyxQID(QidType.QTFILE, 0x80, 0x90),
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
                serializer.getMessageSize(new StyxTWriteMessage(0x1234, 0x3333,
                        new byte[]{0x1, 0x2, 0x3, 0x4},
                        0,
                        4
                        )));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 8 + 4,
                serializer.getMessageSize(new StyxTReadMessage(0x1111, 0x2222, 0x3333)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxRWriteMessage(0x1111, 0x2222)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 51,
                serializer.getMessageSize(new StyxRStatMessage(0x1111, StyxStat.EMPTY)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 4,
                serializer.getMessageSize(new StyxRReadMessage(0x1111, new byte[]{0x1, 0x2, 0x3, 0x4}, 4)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2,
                serializer.getMessageSize(new StyxTFlushMessage(0x1111)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 4,
                serializer.getMessageSize(messageFactory.constructTVersion(0x1111, "ABCD")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize() + 4,
                serializer.getMessageSize(new StyxROpenMessage(0x1111, StyxQID.EMPTY, 0, false)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize() + 4,
                serializer.getMessageSize(new StyxROpenMessage(0x1111, StyxQID.EMPTY, 0, true)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 4,
                serializer.getMessageSize(messageFactory.constructRVersion(0x1111, "ABCD")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2 + serializer.getQidSize(),
                serializer.getMessageSize(new StyxRWalkMessage(0x1111, Collections.singletonList(StyxQID.EMPTY))));

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
        var qid = new StyxQID(
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
    void testSerializeStat() throws StyxException {
        StyxStat stat = new StyxStat(
                (short) 1,
                2,
                new StyxQID(QidType.QTFILE, 0x80, 0x90),
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