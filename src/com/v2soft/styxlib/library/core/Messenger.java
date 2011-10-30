package com.v2soft.styxlib.library.core;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collection;
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
    private Map<Integer, MessageListenerPairs> mMessages = new HashMap<Integer, MessageListenerPairs>();
    private Thread mThread;

    private Socket mSocket;
    private ActiveTags mActiveTags = new ActiveTags();
    private BufferedOutputStream mOutputStream;
    private boolean isWorking;

    public Messenger(Socket socket) throws IOException
    {
        mSocket = socket;
        mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
        mThread = new Thread(this);
        mThread.start();
    }

    // TODO may we can hide ActiveTags?
    public ActiveTags getActiveTags()
    {
        return mActiveTags;
    }

    public boolean send(StyxTMessage message)
    {
        return send(message, 
                new MessageListenerPairs(message));
    }

    public boolean send(StyxTMessage message,
            MessageReceivedListener listener)
    {
        return send(message,
                new MessageListenerPairs(message, listener));
    }

    public boolean send(StyxTMessage message,
            Collection<MessageReceivedListener> listeners)
    {
        return send(message,
                new MessageListenerPairs(message, listeners));
    }

    private boolean send(StyxTMessage message, 
            MessageListenerPairs pairs) {
        try
        {
            if ( Config.LOG_TMESSAGES) {
                System.out.println("Send message "+message.toString());
            }
            mMessages.put(message.getTag(), pairs);
            message.writeToStream(mOutputStream);
            mOutputStream.flush();
            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addListenerFor(StyxMessage message, 
            MessageReceivedListener listener)
    {
        int tag = message.getTag();
        if (!mMessages.containsKey(tag))
            return false;

        MessageListenerPairs pairs = mMessages.get(tag);
        return pairs.addListener(listener);
    }

    @Override
    public void run() 
    {
        try
        {
            StyxInputStream is = new StyxInputStream(mSocket.getInputStream());
            isWorking = true;
            while (isWorking)
            {
                if (Thread.interrupted())
                    throw new InterruptedException();
                try	{
                    StyxMessage message = StyxMessage.factory(is);
                    if ( Config.LOG_RMESSAGES) {
                        System.out.println("Got message "+message.toString());
                    }
                    processIncomingMessage(message);
                } 
                catch (SocketTimeoutException e) { 
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

        MessageListenerPairs pairs = mMessages.get(tag);
        pairs.processListeners(message);
        pairs.getTMessage().setAnswer(message);
        mMessages.remove(tag);
        mActiveTags.releaseTag(tag);
    }

    public class ActiveTags {
        private LinkedList<Integer> mAvailableTags
        = new LinkedList<Integer>();
        private int mLastTag = 0;
        private Object mSync = new Object();

        private ActiveTags()
        { }

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
        isWorking = false;
        mThread.interrupt();		
    }

}
