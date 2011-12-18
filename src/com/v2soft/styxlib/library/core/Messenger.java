package com.v2soft.styxlib.library.core;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;

public class Messenger implements Runnable, Closeable {
    public interface StyxMessengerListener {
        void onSocketDisconected();
    }
    private StyxMessengerListener mListener;
    private Map<Integer, StyxTMessage> mMessages = new HashMap<Integer, StyxTMessage>();
    private Thread mThread;
    private Socket mSocket;
    private ActiveTags mActiveTags = new ActiveTags();
    private BufferedOutputStream mOutputStream;
    private boolean isWorking;
    private int mIOBufferSize;

    public Messenger(Socket socket, int io_unit, StyxMessengerListener listener) 
            throws IOException {
        mIOBufferSize = io_unit;
        mSocket = socket;
        mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
        mListener = listener;
        mThread = new Thread(this);
        mThread.start();
    }

    /**
     * Send messatge to server
     * @param message
     * @return true if success
     */
    public boolean send(StyxTMessage message) {
        try {
            if ( Config.LOG_TMESSAGES) {
                System.out.println("Send message "+message.toString());
            }
            if ( !mSocket.isConnected() ) {
                throw new IOException("Not connected");
            }
            int tag = mActiveTags.getTag();
            message.setTag((short) tag);
            mMessages.put(tag, message);
            message.writeToStream(mOutputStream);
            mOutputStream.flush();
            return true;
        } catch (SocketException e) {
            mListener.onSocketDisconected();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run() {
        try {
            final StyxInputStream is = new StyxInputStream(mSocket.getInputStream());
            isWorking = true;
            while (isWorking) {
                if (Thread.interrupted()) break;
                try	{
                    final StyxMessage message = StyxMessage.factory(is, mIOBufferSize);
                    if ( message != null ) {
                        if ( Config.LOG_RMESSAGES) {
                            System.out.println("Got message "+message.toString());
                        }
                        processIncomingMessage(message);
                    } else {
                        System.out.println("Got NULL message");
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

}
