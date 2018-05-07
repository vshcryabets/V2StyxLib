package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.InetAddress;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TCP channels tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TCPServerChannelTests {

    @Test
    public void testPrepareSocket() {
        Executable closureContainingCodeToTest = new Executable() {
            @Override
            public void execute() throws Throwable {
                new TCPServerChannelDriver(InetAddress.getLoopbackAddress(), 1).prepareSocket();
            }
        };
        assertThrows(SocketException.class, closureContainingCodeToTest, "Socket excpetion should be thrown here");
    }

}
