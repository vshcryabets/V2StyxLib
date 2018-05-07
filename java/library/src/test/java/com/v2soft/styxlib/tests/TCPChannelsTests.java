package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.Connection;
import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.messages.StyxTVersionMessage;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

/**
 * TCP channels tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TCPChannelsTests {

    @Test
    public void testServerClientConnect() throws IOException, InterruptedException {
        TCPServerChannelDriver server = new TCPServerChannelDriver(InetAddress.getByName("127.0.0.1"), 22345);
        TCPClientChannelDriver client = new TCPClientChannelDriver(InetAddress.getByName("127.0.0.1"), 22345);
        server.setRMessageHandler(new RMessagesProcessor("test1"));
        server.setTMessageHandler(new RMessagesProcessor("test2"));
        server.start(128);
        Thread.sleep(200);
        client.setRMessageHandler(new RMessagesProcessor("test3"));
        client.setTMessageHandler(new RMessagesProcessor("test4"));
        client.start(96);
        client.sendMessage(new StyxTVersionMessage(96, Connection.PROTOCOL),
                client.getClients().iterator().next());
    }

}
