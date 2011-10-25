package com.v2soft.styxandroid.library.core;

import java.util.ArrayList;
import java.util.Collection;

import com.v2soft.styxandroid.library.exceptions.StyxException;
import com.v2soft.styxandroid.library.messages.StyxRErrorMessage;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.StyxTMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;

public class MessageListenerPairs {
	private StyxTMessage mTMessage;
	private ArrayList<MessageReceivedListener> mListeners
		= new ArrayList<MessageReceivedListener>();
	
	private Object mSync = new Object();
	
	public MessageListenerPairs(StyxTMessage tMessage)
	{
		mTMessage = tMessage;
	}
	
	public MessageListenerPairs(StyxTMessage tMessage, 
			MessageReceivedListener listener)
	{
		mTMessage = tMessage;
		mListeners.add(listener);
	}
	
	public MessageListenerPairs(StyxTMessage tMessage,
			Collection<MessageReceivedListener> collection)
	{
		mTMessage = tMessage;
		mListeners.addAll(collection);
	}
	
	public StyxTMessage getTMessage()
	{
		return mTMessage;
	}
	
	public boolean addListener(MessageReceivedListener listener)
	{
		synchronized (mSync)
		{
			return mListeners.add(listener);
		}
	}
	
	public boolean removeListener(MessageReceivedListener listener)
	{
		synchronized (mSync)
		{
			return mListeners.remove(listener);
		}
	}

	public void clearListeners()
	{
		synchronized (mSync)
		{
			mListeners.clear();
		}
	}
	
	public void processListeners(StyxMessage rMessage) 
	{
		synchronized (mSync)
		{
			StyxMessage tMessage = getTMessage();
			if (rMessage.getType() == MessageType.Rerror)
			{
				StyxRErrorMessage rError = (StyxRErrorMessage) rMessage;
				for (MessageReceivedListener listener : mListeners)
					listener.onError(tMessage, rError);
			} else {
				for (MessageReceivedListener listener : mListeners)
				{
					try
					{
						listener.onReceived(tMessage, rMessage);
					} catch (StyxException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
