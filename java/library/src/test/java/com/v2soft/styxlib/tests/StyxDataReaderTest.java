package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.io.StyxByteBufferReadable;
import com.v2soft.styxlib.io.StyxDataReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * {@link com.v2soft.styxlib.io.StyxDataReader} test.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxDataReaderTest {

    @Test
    public void testReadUuint64() throws IOException {
        StyxByteBufferReadable buffer = new StyxByteBufferReadable(16);
        buffer.write(new byte[]{8, 7, 6, 5, 4, 3, 2, 1}, 0, 8);
        StyxDataReader reader = new StyxDataReader(buffer);
        Assertions.assertEquals(0x0102030405060708L, reader.readUInt64());
    }

}
