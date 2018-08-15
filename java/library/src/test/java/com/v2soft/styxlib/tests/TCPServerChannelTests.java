package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.messages.StyxTVersionMessage;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import com.v2soft.styxlib.types.ConnectionDetails;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * TCP channels tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TCPServerChannelTests {

    @Test
    public void testPrepareSocketWrongPort() {
        try {
            new TCPServerChannelDriver("localhost", 1).prepareSocket();
            assertTrue(false, "Socket exception should be thrown here");
        } catch (StyxException error) {
            assertEquals(StyxException.DRIVER_BIND_ERROR, error.getInternalCode(), "Wrong error code");
        }
    }

    @Test
    public void testPrepareSocketWrongAddress() {
        try {
            new TCPServerChannelDriver("github.com", 10240).prepareSocket();
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

    @Test
    public void testSocketBusy () throws StyxException, InterruptedException, UnknownHostException {
        TCPServerChannelDriver driver = new TCPServerChannelDriver("0.0.0.0", 10240);
        driver.prepareSocket();
        Thread.sleep(2500);
        Socket test = new Socket();
        try {
            test.bind(new InetSocketAddress(InetAddress.getLocalHost(), 10240));
            assertTrue(false, "Socket exception should be thrown here");
        } catch (IOException e) {
            e.printStackTrace();
        }
        driver.closeSocket();
    }

    @Test
    public void testSocketReceive () throws StyxException, InterruptedException, IOException {
        final byte[] senddata = new byte[]{0, 0, 0, 0, 1, 2};
        senddata[0] = (byte) senddata.length;
        final byte[] recvdata = new byte[senddata.length];
        final int iounit = 128;
        final boolean[] checks = new boolean[10];

        TCPServerChannelDriver driver = new TCPServerChannelDriver("0.0.0.0", 10240){
            @Override
            protected StyxMessage parseMessage(IStyxDataReader reader) throws IOException {
                long length = reader.getUInt32();
                if (length != recvdata.length) {
                    throw new JUnitException("Something wrong with received data");
                }
                reader.read(recvdata, 0, (int) length);
                checks[0] = true;
                return new StyxTVersionMessage(iounit, "testAnswer");
            }
        };
        driver.setTMessageHandler(
                new TMessagesProcessor("test1", new ConnectionDetails("test", iounit), null){
                    @Override
                    public void addClient(ClientDetails clientDetails) {
                        checks[1] = true;
                    }
                });
        driver.setRMessageHandler(
                new RMessagesProcessor("test2"));
        driver.start(iounit);
        Thread.sleep(500);

        Socket test = new Socket();
        test.connect(new InetSocketAddress(InetAddress.getLocalHost(), 10240));
        OutputStream out = test.getOutputStream();
        out.write(senddata);
        out.flush();

        Thread.sleep(500);
        driver.close();

        assertTrue(checks[0], "No message for TMessageHandler");
//        Assert.assertTrue("No readables", checks[2]);
        assertTrue(checks[1], "Not called TMessageHandler::addClient");
        assertArrayEquals(senddata, recvdata, "Wrong recv data");
    }

}
