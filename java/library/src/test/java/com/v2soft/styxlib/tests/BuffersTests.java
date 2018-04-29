package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.io.StyxByteBufferReadable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

/**
 * Styx buffers tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class BuffersTests {

    @Test
    public void testBufferRead() throws IOException {
        Random random = new Random();
        int maxSize = 8192;
        StyxByteBufferReadable readableBuffer = new StyxByteBufferReadable(maxSize);
        byte[] testBuffer = new byte[maxSize];
        byte[] readBuffer = new byte[maxSize];
        random.nextBytes(testBuffer);

        for (int i = 1; i < maxSize; i++) {
            readableBuffer.write(testBuffer, 0, i);
            int read = readableBuffer.read(readBuffer, 0, i);
            Assertions.assertEquals(i, read, "Wrong read bytes count");
            // check read bytes
            for (int j = 0; j < i; j++) {
                if (readBuffer[j] != testBuffer[j]) {
                    Assertions.assertTrue(false, "Wrong byte at position " + j);
                }
            }
        }
    }

}
