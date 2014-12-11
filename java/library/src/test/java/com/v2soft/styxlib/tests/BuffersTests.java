package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.io.StyxByteBufferReadable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Styx buffers tests.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class BuffersTests {

    private static final int PORT = 12395;
    private static final long TIME_INTERVAL = 1000;

    @Before
    public void setUp() {
    }

    @After
    public void shutDown(){

    }

    //
    @Test
    public void testBufferRead() throws IOException {
        Random random = new Random();
        int maxSize = 8192;
        StyxByteBufferReadable readableBuffer = new StyxByteBufferReadable(maxSize);
        byte [] testBuffer = new byte[maxSize];
        byte [] readBuffer = new byte[maxSize];
        random.nextBytes(testBuffer);

        for ( int i = 1; i < maxSize; i++ ) {
            readableBuffer.write(testBuffer, 0, i);
            int read = readableBuffer.read(readBuffer, 0, i);
            assertEquals("Wrong read bytes count", i, read);
            // check read bytes
            for ( int j = 0; j < i; j++ ) {
                if ( readBuffer[j] != testBuffer[j]) {
                    assertTrue("Wrong byte at position "+j, false);
                }
            }
        }
    }

}
