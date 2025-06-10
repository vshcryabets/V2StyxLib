package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BufferWritterImplTest {
    private BufferWritterImpl writer;

    @BeforeEach
    void setUp() {
        writer = new BufferWritterImpl(32);
    }

    @Test
    void testWriteUInt8() {
        writer.writeUInt8((short) 0xAB);
        ByteBuffer buf = writer.getBuffer();
        buf.flip();
        assertEquals((byte) 0xAB, buf.get());
    }

    @Test
    void testWriteUInt16() {
        writer.writeUInt16(0xBEEF);
        ByteBuffer buf = writer.getBuffer();
        buf.flip();
        assertEquals((byte) 0xEF, buf.get());
        assertEquals((byte) 0xBE, buf.get());
    }

    @Test
    void testWriteUInt32() {
        writer.writeUInt32(0xCAFEBABE);
        ByteBuffer buf = writer.getBuffer();
        buf.flip();
        assertEquals((byte) 0xBE, buf.get());
        assertEquals((byte) 0xBA, buf.get());
        assertEquals((byte) 0xFE, buf.get());
        assertEquals((byte) 0xCA, buf.get());
    }

    @Test
    void testWriteUInt64() {
        writer.writeUInt64(0x1122334455667788L);
        ByteBuffer buf = writer.getBuffer();
        buf.flip();
        assertEquals((byte) 0x88, buf.get());
        assertEquals((byte) 0x77, buf.get());
        assertEquals((byte) 0x66, buf.get());
        assertEquals((byte) 0x55, buf.get());
        assertEquals((byte) 0x44, buf.get());
        assertEquals((byte) 0x33, buf.get());
        assertEquals((byte) 0x22, buf.get());
        assertEquals((byte) 0x11, buf.get());
    }

    @Test
    void testWriteUTFString() throws StyxException {
        String str = "Test";
        writer.writeUTFString(str);
        ByteBuffer buf = writer.getBuffer();
        buf.flip();
        int len = (buf.get() & 0xFF) | ((buf.get() & 0xFF) << 8);
        assertEquals(str.length(), len);
        byte[] strBytes = new byte[len];
        buf.get(strBytes);
        assertEquals(str, new String(strBytes, StandardCharsets.UTF_8));
    }

    @Test
    void testWriteAndPrepareBuffer() {
        writer.writeUInt8((short) 0x01);
        writer.prepareBuffer(2);
        assertEquals(0, writer.getBuffer().position());
        assertEquals(2, writer.getBuffer().limit());
    }

    @Test
    void testWriteBufferOverflow() {
        BufferWritterImpl smallWriter = new BufferWritterImpl(2);
        byte[] data = {1, 2, 3};
        assertThrows(java.nio.BufferOverflowException.class, () -> smallWriter.write(data, 0, data.length));
    }
}