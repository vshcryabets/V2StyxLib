package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
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
    protected StyxMessengerListener mListener;
    protected Map<Integer, StyxTMessage> mMessages = new HashMap<Integer, StyxTMessage>();
    protected MessageTagPoll mActiveTags = new MessageTagPoll();
    protected int mTransmittedCount, mErrorCount;
    protected IChannelDriver mDriver;
    protected IMessageProcessor mMessageProcessor;

    public Messenger(IChannelDriver driver, StyxMessengerListener listener)
            throws IOException {
        resetStatistics();
        mDriver = driver;
        mListener = listener;
    }

    public void start(boolean startDriver, int iounit) {
        mMessageProcessor = getMessageProcessor();;
        mDriver.setRMessageHandler(mMessageProcessor);
        if (startDriver && !mDriver.isStarted()) {
            mDriver.start(iounit);
        }
    }

    protected IMessageProcessor getMessageProcessor() {
        RMessagesProcessor rProcessor = new RMessagesProcessor();
        rProcessor.setActiveTags(mActiveTags);
        rProcessor.setListener(mListener);
        rProcessor.setMessagesMap(mMessages);
        return rProcessor;
    }

    private void resetStatistics() {
        mTransmittedCount = 0;
        mErrorCount = 0;
    }

    /**
     * Send message to server
     * @param message message to send
     * @return true if success
     */
    public boolean sendMessage(StyxMessage message, ClientDetails recepient) throws IOException {
        if ( recepient == null ) {
            throw new NullPointerException("Recepient is null");
        }
        try {
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

            mDriver.sendMessage(message, recepient);
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
}