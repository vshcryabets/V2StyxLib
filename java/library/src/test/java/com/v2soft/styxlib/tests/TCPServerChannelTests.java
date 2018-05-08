package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TCP channels tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TCPServerChannelTests {

    @Test
    public void testPrepareSocket() {
        try {
            new TCPServerChannelDriver(InetAddress.getLoopbackAddress(), 1).prepareSocket();
            assertTrue(false, "Socket exception should be thrown here");
        } catch (StyxException error) {
            assertEquals(StyxException.DRIVER_BIND_ERROR, error.getInternalCode(), "Wrong error code");
        }
    }

}
