/*
 * Copyright (C) 2012-2014 V.Shcryabets (vshcryabets@gmail.com)
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
public abstract class UDPAbstractDiscoveryClient extends DiscoveryClient {
    public interface UDPBroadcastListener {
        void onDiscoveryStarted();
        void onDiscoveryFinished();
        void onNewServer(Object item);
        void onException(Throwable error);
    }

    protected static final String LOG_TAG = UDPAbstractDiscoveryClient.class.getSimpleName();

    protected UDPBroadcastListener mListener;
    private Thread mSenderThread = null;
    private Thread mReceiverThread = null;
    private DatagramSocket mSocket;

    protected int mTargetPort;
    private InetAddress mTargetAddresses[];
    protected boolean isWorking;

    /**
     *
     */
    public UDPAbstractDiscoveryClient(int targetPort,
                                      InetAddress targetAddress[]) {
        super();
        mTargetAddresses = targetAddress;
        mTargetPort = targetPort;
    }

    /**
     * Start searching for a other hosts
     */
    public void startDiscovery() {
        if ( mSenderThread != null ) {
            throw new IllegalStateException("Broadcast discovery process already started");
        }
        // start
        mSenderThread = new Thread(mBackgroundSender, LOG_TAG);
        mSenderThread.start();
    }

    /**
     * Stop searching of other hosts
     */
    public void stopDiscovery() {
        isWorking = false;
    }

    public boolean isDiscovering() {
        return isWorking;
    }

    protected abstract DatagramPacket prepareRequest();
    protected abstract void handleAnswer(DatagramPacket income);

    public UDPBroadcastListener getListener() {
        return mListener;
    }

    public void setListener(UDPBroadcastListener mListener) {
        this.mListener = mListener;
    }

    private Runnable mBackgroundSender = new Runnable() {
        @Override
        public void run() {
            isWorking = true;
            startDiscoverySync();
            mSenderThread = null;
        }
    };

    private Runnable mBackgroundReceiver = new Runnable() {
        @Override
        public void run() {
            final byte[] buf = new byte[256];

            while ( isWorking ) {
                try {
                    final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    mSocket.receive(packet);
                    handleAnswer(packet);
                    Thread.sleep(100);
                } catch (IOException e) {
                    onError(e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    };

    public void startDiscoverySync() {
        isWorking = true;
        // start receiver thread
        mReceiverThread = new Thread(mBackgroundReceiver,
                UDPAbstractDiscoveryClient.class.getSimpleName()+"R");
        mReceiverThread.start();
        if (mListener != null) {
            mListener.onDiscoveryStarted();
        }

        int count = getRetryCount();
        try {
            mSocket = new DatagramSocket();
            mSocket.setSoTimeout(getDelayBetweenRetry());

            while ( count -- > 0 && isWorking ) {
                // send packet
                final DatagramPacket packet = prepareRequest();
                for ( InetAddress address : mTargetAddresses) {
                    packet.setAddress(address);
                    packet.setPort(mTargetPort);
                    try {
                        mSocket.send(packet);
                    } catch (Exception e) {
                        onError(e);
                    }
                }
                // delay
                Thread.sleep(getDelayBetweenRetry());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            onError(e);
        } finally {
            mSocket.close();
        }
        isWorking = false;
        if ( mReceiverThread.isAlive() ) {
            try {
                mReceiverThread.join();
            } catch (InterruptedException e) {
            }
        }
        mReceiverThread = null;
        if (mListener != null) {
            mListener.onDiscoveryFinished();
        }
    }

    public void onError(Exception e) {
        e.printStackTrace();
        if ( mListener != null ) {
            mListener.onException(e);
        }
    }
}
