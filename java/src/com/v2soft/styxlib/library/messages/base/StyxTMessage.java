package com.v2soft.styxlib.library.messages.base;

import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.exceptions.StyxWrongMessageException;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public abstract class StyxTMessage extends StyxMessage {
	private StyxMessage mAnswer;
	
	public StyxTMessage(MessageType type) {
		super(type);
	}
	
	public StyxTMessage(MessageType type, int tag) {
		super(type, tag);
	}
	
	public StyxMessage getAnswer() {
		return mAnswer;
	}
	
	public void setAnswer(StyxMessage answer) 
		throws StyxException {
		synchronized (this) {
			if (!checkAnswer(answer))
				throw new StyxWrongMessageException(answer, getNeeded());
			mAnswer = answer;
			notifyAll();
		}
	}
	
	public StyxMessage waitForAnswer(long timeout) throws InterruptedException, TimeoutException {
		synchronized (this)	{
		    if ( mAnswer == null)
				wait(timeout);
		    if ( mAnswer == null )
		        throw new TimeoutException();
			return mAnswer;
		}
	}
	
	protected boolean checkAnswer(StyxMessage answer)
	{
		MessageType needed = getNeeded();
		MessageType received = answer.getType();
		
		return (needed == received || received == MessageType.Rerror);
	}
	
	protected abstract MessageType getNeeded();
}
