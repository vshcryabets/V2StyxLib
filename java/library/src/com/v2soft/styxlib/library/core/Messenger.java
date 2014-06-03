package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.io.StyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.IMessageTransmitter;
import com.v2soft.styxlib.library.server.MessagesProcessor;
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
    private int mTransmitedCount, mReceivedCount, mErrorCount, mBuffersAllocated;
    protected ILogListener mLogListener;
    protected IClientChannelDriver mDriver;
    protected IMessageProcessor mTMessageProcessor;

    public Messenger(IClientChannelDriver driver, int io_unit, StyxMessengerListener listener,
                     ILogListener logListener)
            throws IOException {
        mLogListener = logListener;
        resetStatistics();
        mIOBufferSize = io_unit;
        mDriver = driver;
        mListener = listener;
        mDriver.setMessageHandler(mMessageHandler);
        mDriver.start();
    }

    public void export(IVirtualStyxFile root, String protocol) {
        if ( root != null ) {
            mTMessageProcessor = new MessagesProcessor(mIOBufferSize, root, protocol);
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

    private final IMessageProcessor mMessageHandler = new IMessageProcessor() {
        @Override
        public void addClient(ClientState state) {
            if ( mTMessageProcessor != null ) {
                mTMessageProcessor.addClient(state);
            }
        }

        @Override
        public void removeClient(ClientState state) {
            if ( mTMessageProcessor != null ) {
                mTMessageProcessor.removeClient(state);
            }
        }

        @Override
        public void processPacket(ClientState client, StyxMessage message) {
            if ( mLogListener != null ) {
                mLogListener.onMessageReceived(message);
            }
            mReceivedCount++;
            if ( message.getType().isTMessage() ) {
                if ( mTMessageProcessor != null ) {
                    try {
                        mTMessageProcessor.processPacket(client, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                int tag = message.getTag();
                if (!mMessages.containsKey(tag)) // we didn't send T message with such tag, so ignore this R message
                    return;
                final StyxTMessage tMessage = mMessages.get(tag);
                if (tMessage.getType() == MessageType.Tclunk ||
                        tMessage.getType() == MessageType.Tremove) {
                    mListener.onFIDReleased(((StyxTMessageFID) tMessage).getFID());
                }
                try {
                    tMessage.setAnswer(message);
                } catch (StyxException e) {
                    e.printStackTrace();
                }
                if (message.getType() == MessageType.Rerror) {
                    mErrorCount++;
                }
                mMessages.remove(tag);
                mActiveTags.releaseTag(tag);
            }
        }

        @Override
        public void close() throws IOException {
            if ( mTMessageProcessor != null ) {
                mTMessageProcessor.close();
            }
        }
    };

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
