package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.IStyxBuffer;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.io.StyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ObjectsPoll;
import com.v2soft.styxlib.library.types.ObjectsPoll.ObjectsPollFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Messenger implements Runnable, Closeable, ObjectsPollFactory<IStyxDataWriter> {
    public interface StyxMessengerListener {
        void onSocketDisconected();
        void onTrashReceived();
        void onFIDReleased(long fid);
    }
    private StyxMessengerListener mListener;
    private Map<Integer, StyxTMessage> mMessages = new HashMap<Integer, StyxTMessage>();
    private Thread mThread;
    private SocketChannel mSocketChannel;
    private ActiveTags mActiveTags = new ActiveTags();
    private boolean isWorking;
    private int mIOBufferSize;
    private int mTransmitedCount, mReceivedCount, mErrorCount, mBuffersAllocated;
    private ObjectsPoll<IStyxDataWriter> mBufferPoll;
    protected ILogListener mLogListener;

    public Messenger(SocketChannel socket, int io_unit, StyxMessengerListener listener,
                     ILogListener logListener)
            throws IOException {
        mLogListener = logListener;
        resetStatistics();
        mIOBufferSize = io_unit;
        mSocketChannel = socket;
        mListener = listener;
        mBufferPoll = new ObjectsPoll<IStyxDataWriter>(this);
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
            if ( mLogListener != null ) {
                mLogListener.onSendMessage(message);
            }
            if ( !mSocketChannel.isConnected()) throw new IOException("Not connected to server");

            int tag = StyxMessage.NOTAG;
            if ( message.getType() != MessageType.Tversion ) {
                tag = mActiveTags.getTag();
            }
            message.setTag((short) tag);
            mMessages.put(tag, message);

            IStyxDataWriter buffer = mBufferPoll.get();
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
            final StyxByteBufferReadable buffer = new StyxByteBufferReadable(mIOBufferSize*2);
            final StyxDataReader reader = new StyxDataReader(buffer);
            isWorking = true;
            while (isWorking) {
                if (Thread.interrupted()) break;
                // read from socket
                try {
                    int readed = buffer.readFromChannel(mSocketChannel);
                    if ( readed > 0 ) {
                        // try to decode
                        final int inBuffer = buffer.remainsToRead();
                        if ( inBuffer > 4 ) {
                            final long packetSize = reader.getUInt32();
                            if ( inBuffer >= packetSize ) {
                                final StyxMessage message = StyxMessage.factory(reader, mIOBufferSize);
                                if ( mLogListener != null ) {
                                    mLogListener.onMessageReceived(message);
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
                } catch (ClosedByInterruptException e) {
                    // finish
                    break;
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
        final StyxTMessage tMessage = mMessages.get(tag);
        if ( tMessage.getType() == MessageType.Tclunk ||
                tMessage.getType() == MessageType.Tremove ) {
            mListener.onFIDReleased(((StyxTMessageFID)tMessage).getFID());
        }
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
    public IStyxDataWriter create() {
        mBuffersAllocated++;
        return new StyxDataWriter(ByteBuffer.allocateDirect(mIOBufferSize));
    }

}
