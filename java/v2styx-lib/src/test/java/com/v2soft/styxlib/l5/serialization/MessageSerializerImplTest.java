package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.StyxTVersionMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageSerializerImplTest {

    @Test
    public void testSerializationTMessages() throws IOException {
        MessageSerializer serializer = new MessageSerializerImpl();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        BufferWritter outputBuffer = new ByteBufferWritter(buffer);
        serializer.serialize(new StyxTVersionMessage(128, "9P2000"), outputBuffer);
        byte[] data = new byte[buffer.limit()];
        buffer.position(0);
        buffer.get(data);
        Assertions.assertArrayEquals(new byte[]{19, 0, 0, 0,
                (byte) MessageType.Tversion.getByte(),
                (byte) 0xFF, (byte) 0xFF, // tag
                (byte) 0x80, 0, 0, 0,  // io unit
                // proto string size
                0x06, 0x00,
                // proto string
                0x39, 'P', 0x32, 0x30, 0x30, 0x30}, data, "TVersion");
    }
}
