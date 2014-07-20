package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.io.StyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.IMessageTransmitter;
import com.v2soft.styxlib.library.server.TMessagesProcessor;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.ObjectsPoll.ObjectsPollFactory;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Messenger implements IMessageTransmitter, ObjectsPollFactory<IStyxDataWriter> {
    public interface StyxMessengerListener {
        void onSocketDisconnected();
        void onTrashReceived();
        void onFIDReleased(long fid);
    }
    private StyxMessengerListener mListener;
    private Map<Integer, StyxTMessage> mMessages = new HashMap<Integer, StyxTMessage>();
    private ActiveTags mActiveTags = new ActiveTags();
    private int mIOBufferSize;
    private int mTransmitedCount, mErrorCount, mBuffersAllocated;
    protected ILogListener mLogListener;
    protected IClientChannelDriver mDriver;
    protected MessagesFilter mFilterProcessor;

    public Messenger(IClientChannelDriver driver, int io_unit, StyxMessengerListener listener,
                     ILogListener logListener)
            throws IOException {
        mLogListener = logListener;
        resetStatistics();
        mIOBufferSize = io_unit;
        mDriver = driver;
        mListener = listener;
        RMessagesProcessor rProcessor = new RMessagesProcessor();
        rProcessor.setmActiveTags(mActiveTags);
        rProcessor.setmListener(mListener);
        rProcessor.setmLogListener(mLogListener);
        rProcessor.setmMessagesMap(mMessages);
        mFilterProcessor = new MessagesFilter(null, rProcessor);
        mDriver.setMessageHandler(mFilterProcessor);
        mDriver.start();
    }

    public void export(IVirtualStyxFile root, String protocol) {
        IMessageProcessor mTMessageProcessor = mFilterProcessor.getTProcessor();

        if ( root != null ) {
            // if root was changed we should close previous TProcessor
            mTMessageProcessor = new TMessagesProcessor(mIOBufferSize, root, protocol);
        } else {
            if ( mTMessageProcessor != null ) {
                try {
                    mTMessageProcessor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mTMessageProcessor = null;
            }
        }
        mFilterProcessor.setTProcessor(mTMessageProcessor);
    }

    private void resetStatistics() {
        mTransmitedCount = 0;
        mErrorCount = 0;
        mBuffersAllocated = 0;
    }

    /**
     * Send message to server
     * @param message
     * @return true if success
     */
    public boolean sendMessage(StyxMessage message) throws IOException {
        try {
            if ( mLogListener != null ) {
                mLogListener.onSendMessage(message);
            }
            if ( !mDriver.isConnected()) throw new IOException("Not connected to server");

            if ( message.getType().isTMessage() ) {
                // set message tag
                int tag = StyxMessage.NOTAG;
                if (message.getType() != MessageType.Tversion) {
                    tag = mActiveTags.getTag();
                }
                message.setTag((short) tag);
                mMessages.put(tag, (StyxTMessage)message);
            }

            mDriver.sendMessage( message);
            mTransmitedCount++;
            return true;
        } catch (SocketException e) {
            mListener.onSocketDisconnected();
        }
        return false;
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
    public void close() throws IOException {
        mDriver.close();
    }
    @Override
    public int getTransmitedCount() {return mTransmitedCount;}
    @Override
    public int getErrorsCount() {return mErrorCount;}
    public int getAllocationCount() {return mBuffersAllocated;}

    @Override
    public IStyxDataWriter create() {
        mBuffersAllocated++;
        return new StyxDataWriter(ByteBuffer.allocateDirect(mIOBufferSize));
    }

}
