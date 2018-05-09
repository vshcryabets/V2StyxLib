package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TCP channels tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TCPServerChannelTests {

    @Test
    public void testPrepareSocketWrongPort() {
        try {
            new TCPServerChannelDriver("127.0.0.1", 1).prepareSocket();
            assertTrue(false, "Socket exception should be thrown here");
        } catch (StyxException error) {
            assertEquals(StyxException.DRIVER_BIND_ERROR, error.getInternalCode(), "Wrong error code");
        }
    }

    @Test
    public void testPrepareSocketWrongAddress() {
        try {
            new TCPServerChannelDriver("1.1.1.1", 10240).prepareSocket();
            assertTrue(false, "Socket exception should be thrown here");
        } catch (StyxException error) {
            assertEquals(StyxException.DRIVER_BIND_ERROR, error.getInternalCode(), "Wrong error code");
        }
    }

    @Test
    public void testPrepareSocketIncorrectAddress() {
        try {
            new TCPServerChannelDriver("qwewqe", 10240).prepareSocket();
            assertTrue(false, "Socket exception should be thrown here");
        } catch (StyxException error) {
            assertEquals(StyxException.DRIVER_CANT_RESOLVE_NAME, error.getInternalCode(), "Wrong error code");
        }
    }

}
