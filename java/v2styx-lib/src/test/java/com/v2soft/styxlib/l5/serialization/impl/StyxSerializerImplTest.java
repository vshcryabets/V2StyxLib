package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.QIDType;
import com.v2soft.styxlib.l5.messages.StyxRErrorMessage;
import com.v2soft.styxlib.l5.messages.StyxTAttachMessage;
import com.v2soft.styxlib.l5.messages.StyxTAuthMessage;
import com.v2soft.styxlib.l5.messages.StyxTWalkMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.structs.StyxQID;
import org.junit.jupiter.api.Test;

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

    }
}