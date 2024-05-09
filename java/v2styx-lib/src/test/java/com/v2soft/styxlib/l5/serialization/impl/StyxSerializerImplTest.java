package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QIDType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class StyxSerializerImplTest {

    @Test
    void testGetSize() {
        var serializer = new StyxSerializerImpl();
        assertEquals(StyxMessage.BASE_BINARY_SIZE + 2 + 2,
                serializer.getMessageSize(new StyxRErrorMessage(0, "AB")));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxTMessageFID(MessageType.Tread, MessageType.Rread, 0x2345)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 2 + 2 + 6 + 12,
                serializer.getMessageSize(new StyxTAttachMessage(
                        0x2345,
                        0x3333,
                        "user",
                        "mountpoint")));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + StyxQID.CONTENT_SIZE,
                serializer.getMessageSize(new StyxRSingleQIDMessage(
                        MessageType.Rread,
                        0x10,
                        new StyxQID(QIDType.QTFILE, 1, 2))));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 6 + 12,
                serializer.getMessageSize(new StyxTAuthMessage(
                        0x1234,
                        "user",
                        "mountpoint")));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 4 + 2 + 3 + 3 + 3,
                serializer.getMessageSize(new StyxTWalkMessage(
                        0x1234,
                        0x9876,
                        "a/b/c")));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 1,
                serializer.getMessageSize(new StyxTOpenMessage(
                        0x1234,
                        1)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 5 + 6,
                serializer.getMessageSize(new StyxTCreateMessage(
                        0x1234,
                        "name",
                        0x33,
                        0x22)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 5 + 6,
                serializer.getMessageSize(new StyxTWStatMessage(
                        0x1234,
                        new StyxStat(
                                (short)1,
                                2,
                                new StyxQID(QIDType.QTFILE, 0x80, 0x90),
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

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 5 + 6,
                serializer.getMessageSize(new StyxTWriteMessage(0x1234, 0x3333,
                        new byte[]{0x1, 0x2, 0x3, 0x4},
                        0,
                        4
                        )));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4 + 8 + 4,
                serializer.getMessageSize(new StyxTReadMessage(0x1111, 0x2222, 0x3333)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxRWriteMessage(0x1111, 0x2222)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxRStatMessage(0x1111, StyxStat.EMPTY)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxRReadMessage(0x1111, new byte[]{0x1, 0x2, 0x3, 0x4}, 4)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 2,
                serializer.getMessageSize(new StyxTFlushMessage(0x1111)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 2,
                serializer.getMessageSize(new StyxTVersionMessage(0x1111, "ABCD")));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxROpenMessage(0x1111, StyxQID.EMPTY, 0, false)));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxRVersionMessage(0x1111, "ABCD")));

        assertEquals(StyxMessage.BASE_BINARY_SIZE + 4,
                serializer.getMessageSize(new StyxRWalkMessage(0x1111, Collections.singletonList(StyxQID.EMPTY))));

    }
}