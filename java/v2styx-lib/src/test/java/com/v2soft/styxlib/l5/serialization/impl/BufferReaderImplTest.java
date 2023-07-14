package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.l5.io.Buffer;
import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

class BufferReaderImplTest {

    @Test
    void readUTFString() throws UnsupportedEncodingException {
        BufferImpl buffer = new BufferImpl(128);
        byte[] testData = {0x05, 0x00, 'A', 'B', 'C', 'D', 'E', 'F', 'G'};
        buffer.write(testData, 0, testData.length);
        BufferReaderImpl impl = new BufferReaderImpl(buffer);
        assertEquals("ABCDE", impl.readUTFString());
    }

    @Test
    void readUInt8() {
        BufferImpl buffer = new BufferImpl(128);
        byte[] testData = {0x05, 0x06};
        buffer.write(testData, 0, testData.length);
        BufferReaderImpl impl = new BufferReaderImpl(buffer);
        assertEquals(0x05, impl.readUInt8());
        assertEquals(0x06, impl.readUInt8());
    }

    @Test
    void readUInt16() {
        BufferImpl buffer = new BufferImpl(128);
        byte[] testData = {0x05, 0x06, 0x07, 0x08};
        buffer.write(testData, 0, testData.length);
        BufferReaderImpl impl = new BufferReaderImpl(buffer);
        assertEquals(0x0605, impl.readUInt16());
        assertEquals(0x0807, impl.readUInt16());
    }

    @Test
    void readUInt32() {
        BufferImpl buffer = new BufferImpl(128);
        byte[] testData = {0x05, 0x06, 0x07, 0x08, 0x10, 0x11, 0x12, 0x13};
        buffer.write(testData, 0, testData.length);
        BufferReaderImpl impl = new BufferReaderImpl(buffer);
        assertEquals(0x08070605, impl.readUInt32());
        assertEquals(0x13121110, impl.readUInt32());
    }

    @Test
    void readUInt64() {
        BufferImpl buffer = new BufferImpl(128);
        byte[] testData = {0x05, 0x06, 0x07, 0x08, 0x10, 0x11, 0x12, 0x13,
                0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27};
        buffer.write(testData, 0, testData.length);
        BufferReaderImpl impl = new BufferReaderImpl(buffer);
        assertEquals(0x1312_1110_0807_0605L, impl.readUInt64());
        assertEquals(0x2726_2524_2322_2120L, impl.readUInt64());
    }
}