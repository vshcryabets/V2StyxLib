package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StyxSerializerImplTest {
    StyxSerializerImpl serializer = new StyxSerializerImpl();

    @Test
    void testGetSize() {
        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 2 + 2,
                serializer.getMessageSize(new StyxRErrorMessage(0, "AB")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxTMessageFID(MessageType.Unspecified,
                        MessageType.Rread,
                        0x2345)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 2 + 6 + 12,
                serializer.getMessageSize(new StyxTAttachMessage(
                        0x2345,
                        0x3333,
                        "user",
                        "mountpoint")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize(),
                serializer.getMessageSize(new StyxRSingleQIDMessage(
                        MessageType.Unspecified,
                        0x10,
                        new StyxQID(QidType.QTFILE, 1, 2))));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 6 + 12,
                serializer.getMessageSize(new StyxTAuthMessage(
                        0x1234,
                        "user",
                        "mountpoint")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 6 + 3,
                serializer.getMessageSize(new StyxTAuthMessage(
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
                serializer.getMessageSize(new StyxTVersionMessage(0x1111, "ABCD")));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize() + 4,
                serializer.getMessageSize(new StyxROpenMessage(0x1111, StyxQID.EMPTY, 0, false)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + serializer.getQidSize() + 4,
                serializer.getMessageSize(new StyxROpenMessage(0x1111, StyxQID.EMPTY, 0, true)));

        assertEquals(IDataSerializer.BASE_BINARY_SIZE + 4 + 2 + 4,
                serializer.getMessageSize(new StyxRVersionMessage(0x1111, "ABCD")));

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

        var buffer = ByteBuffer.allocate(8192);
        var outputBuffer = new BufferWritterImpl(buffer);
        outputBuffer.prepareBuffer(serializer.getQidSize());
        serializer.serializeQid(qid, outputBuffer);
        byte[] data = new byte[buffer.limit()];
        buffer.position(0);
        buffer.get(data);
        assertArrayEquals(expected, data);
    }
}