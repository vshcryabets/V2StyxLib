/*
 * Copyright (C) 2012 V.Shcryabets (vshcryabets@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.v2soft.AndLib.networking;

import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public abstract class UDPAbstractDiscoveryServer extends DiscoveryServer implements Closeable{
    protected Thread mReceiverThread = null;
    protected DatagramSocket mSocket;
    protected final InetAddress mAddress;
    protected final int mPort;

    public UDPAbstractDiscoveryServer(int port, InetAddress listenOnInterface) throws SocketException {
        mAddress = listenOnInterface;
        mPort = port;

    }

    public void listen() throws SocketException {
        if ( mReceiverThread != null ) {
            throw new IllegalStateException("Broadcast receiving process already started");
        }
        mReceiverThread = new Thread(mBackgroundReceiver, UDPAbstractDiscoveryServer.class.getSimpleName());
        mSocket = new DatagramSocket(mPort, mAddress);
        mReceiverThread.start();
    }

    public void close() {
        mReceiverThread.interrupt();
        mSocket.close();
    }

    protected abstract void handleIncomePacket(DatagramSocket socket, DatagramPacket income);

    private Runnable mBackgroundReceiver = new Runnable() {
        @Override
        public void run() {
            final byte[] buf = new byte[256];
            MetricsAndStats.byteArrayAllocation++;
            while ( true ) {
                try {
                    final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    mSocket.receive(packet);
                    handleIncomePacket(mSocket, packet);
                    Thread.sleep(100);
                } catch (IOException e) {
                    if ( mSocket.isClosed() ) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            mReceiverThread = null;
            mSocket = null;
        }
    };
}
