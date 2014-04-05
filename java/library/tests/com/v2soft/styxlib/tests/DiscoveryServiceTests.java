package com.v2soft.styxlib.tests;

import com.v2soft.AndLib.networking.DiscoveryClient;
import com.v2soft.AndLib.networking.DiscoveryServer;
import com.v2soft.AndLib.networking.UDPAbstractDiscoveryClient;
import com.v2soft.AndLib.networking.UDPAbstractDiscoveryServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * UDP broadcast discovery service tests.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class DiscoveryServiceTests {

    private static final int PORT = 12395;

    @Before
    public void setUp() {
    }

    @After
    public void shutDown(){

    }

    //
    @Test
    public void testSyncOneTimeAbstractDiscovery() throws IOException {
        final String answerStr = UUID.randomUUID().toString();
        final byte[] answer = answerStr.getBytes("utf-8");
        final byte[] request = UUID.randomUUID().toString().getBytes("utf-8");
        // load addresses
        final ArrayList<InetAddress> listOfBroadcasts = new ArrayList<InetAddress>();
        final ArrayList<InetAddress> listOfAddresses = new ArrayList<InetAddress>();
        for (Enumeration<NetworkInterface> en =
                     NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            System.out.println("Found up interface:" + intf);
            for (InterfaceAddress interfaceAddress : intf.getInterfaceAddresses()) {
                if ( interfaceAddress.getBroadcast() == null ) continue;
                listOfBroadcasts.add(interfaceAddress.getBroadcast());
                listOfAddresses.add(interfaceAddress.getAddress());
            }
        }
        // start server
        DiscoveryServer server = new UDPAbstractDiscoveryServer(PORT, listOfAddresses.get(0)) {
            @Override
            protected void handleIncomePacket(DatagramSocket socket, DatagramPacket packet) {
                InetAddress sourceAddress = packet.getAddress();
                int sourcePort = packet.getPort();
                byte [] data = packet.getData();
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
        final int [] discoveredServicersCount = new int[1];
        // start discovery
        DiscoveryClient explorer = new UDPAbstractDiscoveryClient(PORT,
                listOfBroadcasts.toArray(new InetAddress[listOfBroadcasts.size()])
                ) {
            @Override
            protected DatagramPacket prepareRequest() {
                DatagramPacket requestPacket = new DatagramPacket(request, request.length);
                return requestPacket;
            }
            @Override
            protected void handleAnswer(DatagramPacket income) {
                String answer = new String(income.getData(), 0, income.getLength());
                assertEquals("Wrong answer", answerStr, answer);
                discoveredServicersCount[0]++;
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
        explorer.startDiscoverySync();
        assertEquals("Wrong number of discovered servers", 1, discoveredServicersCount[0] );

        server.close();
    }

}
