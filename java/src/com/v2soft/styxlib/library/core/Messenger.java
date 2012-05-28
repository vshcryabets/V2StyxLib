package com.v2soft.styxlib.library.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.DualStateBuffer;
import com.v2soft.styxlib.library.io.StyxByteBuffer;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ObjectsPoll;
import com.v2soft.styxlib.library.types.ObjectsPoll.ObjectsPollFactory;

public class Messenger implements Runnable, Closeable, ObjectsPollFactory<StyxByteBuffer> {
    public interface StyxMessengerListener {
        void onSocketDisconected();
        void onTrashReceived();
    }
    private StyxMessengerListener mListener;
    private Map<Integer, StyxTMessage> mMessages = new HashMap<Integer, StyxTMessage>();
    private Thread mThread;
    private SocketChannel mSocketChannel;
    private ActiveTags mActiveTags = new ActiveTags();
    private boolean isWorking;
    private int mIOBufferSize;
    private int mTransmitedCount, mReceivedCount, mErrorCount, mBuffersAllocated;
    private ObjectsPoll<StyxByteBuffer> mBufferPoll;

    public Messenger(SocketChannel socket, int io_unit, StyxMessengerListener listener) 
            throws IOException {
        resetStatistics();
        mIOBufferSize = io_unit;
        mSocketChannel = socket;
        mListener = listener;
        mBufferPoll = new ObjectsPoll<StyxByteBuffer>(this);
        mThread = new Thread(this);
        mThread.start();        
    }

    private void resetStatistics() {
        mTransmitedCount = 0;
        mReceivedCount = 0;
        mErrorCount = 0;
        mBuffersAllocated = 0;
    }

    /**
     * Send message to server
     * @param message
     * @return true if success
     */
    public boolean send(StyxTMessage message) throws IOException {
        try {
            if ( Config.LOG_TMESSAGES) {
                System.out.println("Send message "+message.toString());
            }
            assert mSocketChannel.isConnected();

            int tag = StyxMessage.NOTAG;
            if ( message.getType() != MessageType.Tversion ) {
                tag = mActiveTags.getTag();
            }
            message.setTag((short) tag);
            mMessages.put(tag, message);

            StyxByteBuffer buffer = mBufferPoll.get();
            message.writeToBuffer(buffer);
            final ByteBuffer inbuf = buffer.getBuffer();
            inbuf.flip();
            mSocketChannel.write(inbuf);
            mTransmitedCount++;
            return true;
        } catch (SocketException e) {
            mListener.onSocketDisconected();
        }
        return false;
    }

    @Override
    public void run() {
        try {
            final DualStateBuffer buffer = new DualStateBuffer(mIOBufferSize*2);
            isWorking = true;
            while (isWorking) {
                if (Thread.interrupted()) break;
                // read from socket
                try {
                    int readed = buffer.readFromChannel(mSocketChannel);
                    if ( readed > 0 ) {
                        // try to decode
                        int inBuffer = buffer.remainsToRead();
                        if ( inBuffer > 4 ) {
                            long packetSize = buffer.getUInt32();
                            if ( inBuffer >= packetSize ) {
                                StyxMessage message = StyxMessage.factory(buffer, mIOBufferSize);
                                if ( Config.LOG_RMESSAGES) {
                                    System.out.println("Got message "+message.toString());
                                }
                                mReceivedCount++;
                                processIncomingMessage(message);
                            }
                        }
                    }
                }
                catch (SocketTimeoutException e) {
                    // Nothing to read
                    //                    e.printStackTrace();
                }
                catch (StyxException e)	{ 
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    private synchronized void processIncomingMessage(StyxMessage message) 
            throws StyxException {
        int tag = message.getTag();
        if (!mMessages.containsKey(tag)) // we didn't send T message with such tag, so ignore this R message
            return;
        StyxTMessage tMessage = mMessages.get(tag);
        tMessage.setAnswer(message);
        if ( message.getType() == MessageType.Rerror ) {
            mErrorCount++;
        }
        mMessages.remove(tag);
        mActiveTags.releaseTag(tag);
    }

    public class ActiveTags {
        private LinkedList<Integer> mAvailableTags = new LinkedList<Integer>();
        private int mLastTag = 0;
        private Object mSync = new Object();

        public int getTag()
        {
            synchronized (mSync)
            {
                if (!mAvailableTags.isEmpty())
                    return mAvailableTags.poll();

                mLastTag++;
                if (mLastTag > Consts.MAXUSHORT)
                    mLastTag = 0;
                return mLastTag;
            }
        }

        public boolean releaseTag(int tag)
        {
            synchronized (mSync)
            {
                if (tag == StyxMessage.NOTAG)
                    return false;
                return mAvailableTags.add(tag);
            }
        }
    }

    @Override
    public void close() {
        isWorking = false;
        mThread.interrupt();		
    }

    public StyxMessengerListener getListener() {
        return mListener;
    }

    public void setListener(StyxMessengerListener mListener) {
        this.mListener = mListener;
    }

    public int getTransmitedCount() {return mTransmitedCount;}
    public int getReceivedCount() {return mReceivedCount;}
    public int getErrorsCount() {return mErrorCount;}
    public int getAllocationCount() {return mBuffersAllocated;}

    @Override
    public StyxByteBuffer create() {
        mBuffersAllocated++;
        return new StyxByteBuffer(ByteBuffer.allocateDirect(mIOBufferSize));
    }

}
