package com.v2soft.styxlib.tests;

import com.v2soft.AndLib.networking.DiscoveryClient;
import com.v2soft.AndLib.networking.DiscoveryServer;
import com.v2soft.AndLib.networking.UDPAbstractDiscoveryClient;
import com.v2soft.AndLib.networking.UDPAbstractDiscoveryServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UDP broadcast discovery service tests.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
@Disabled
public class DiscoveryServiceTests {

    private static final int PORT = 12395;

    //
    @Test
    public void testSyncOneTimeAbstractDiscovery() throws IOException {
        final String answerStr = UUID.randomUUID().toString();
        final byte[] answer = answerStr.getBytes("utf-8");
        final byte[] request = UUID.randomUUID().toString().getBytes("utf-8");
        // start server
        DiscoveryServer server = new UDPAbstractDiscoveryServer(PORT, InetAddress.getLoopbackAddress()) {
            @Override
            protected void handleIncomePacket(DatagramSocket socket, DatagramPacket packet) {
                InetAddress sourceAddress = packet.getAddress();
                int sourcePort = packet.getPort();
                byte[] data = packet.getData();
                String received = new String(data, 0, packet.getLength());
                try {
                    socket.send(new DatagramPacket(answer, answer.length,
                            packet.getAddress(), packet.getPort()));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        server.listen();
        final int[] flags = new int[]{0, 0, 0};
        // start discovery
        UDPAbstractDiscoveryClient explorer = new UDPAbstractDiscoveryClient(PORT,
                new InetAddress[]{InetAddress.getLoopbackAddress()}) {
            private Set<String> mCodes = new HashSet<String>();

            @Override
            protected DatagramPacket prepareRequest() {
                DatagramPacket requestPacket = new DatagramPacket(request, request.length);
                return requestPacket;
            }

            @Override
            protected void handleAnswer(DatagramPacket income) {
                String answer = new String(income.getData(), 0, income.getLength());
                assertEquals("Wrong answer", answerStr, answer);
                if (!mCodes.contains(answer)) {
                    mCodes.add(answer);
                    if (mListener != null) {
                        mListener.onNewServer(answer);
                    }
                }
            }

            @Override
            public int getRetryCount() {
                return 3;
            }

            @Override
            public int getDelayBetweenRetry() {
                return 1500;
            }
        };
        explorer.setListener(new UDPAbstractDiscoveryClient.UDPBroadcastListener() {
            @Override
            public void onDiscoveryStarted() {
                flags[0]++;
            }

            @Override
            public void onDiscoveryFinished() {
                flags[1]++;
            }

            @Override
            public void onNewServer(Object item) {
                flags[2]++;
            }

            @Override
            public void onException(Throwable error) {

            }
        });
        explorer.startDiscoverySync();
        assertEquals(1, flags[0], "Wrong number of discovery start count");
        assertEquals(1, flags[1], "Wrong number of discovery end count");
        assertEquals(1, flags[2], "Wrong number of discovered servers " + flags[2]);

        server.close();
    }

}
