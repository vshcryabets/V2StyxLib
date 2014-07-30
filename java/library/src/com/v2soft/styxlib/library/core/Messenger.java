package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.IMessageTransmitter;
import com.v2soft.styxlib.library.server.TMessagesProcessor;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.utils.MessageTagPoll;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class Messenger implements IMessageTransmitter {
    public interface StyxMessengerListener {
        void onSocketDisconnected();
        void onTrashReceived();
        void onFIDReleased(long fid);
    }
    private StyxMessengerListener mListener;
    private Map<Integer, StyxTMessage> mMessages = new HashMap<Integer, StyxTMessage>();
    private MessageTagPoll mActiveTags = new MessageTagPoll();
    private int mIOBufferSize;
    private int mTransmittedCount, mErrorCount, mBuffersAllocated;
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
        rProcessor.setActiveTags(mActiveTags);
        rProcessor.setListener(mListener);
        rProcessor.setLogListener(mLogListener);
        rProcessor.setMessagesMap(mMessages);
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
        mTransmittedCount = 0;
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
                    tag = mActiveTags.getFreeItem();
                }
                message.setTag((short) tag);
                mMessages.put(tag, (StyxTMessage)message);
            }

            mDriver.sendMessage( message);
            mTransmittedCount++;
            return true;
        } catch (SocketException e) {
            mListener.onSocketDisconnected();
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        mDriver.close();
    }
    @Override
    public int getTransmittedCount() {return mTransmittedCount;}
    @Override
    public int getErrorsCount() {return mErrorCount;}
    public int getAllocationCount() {return mBuffersAllocated;}
}
