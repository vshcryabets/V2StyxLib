package com.v2soft.styxlib.library.messages.base;

import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.exceptions.StyxWrongMessageException;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTMessage extends StyxMessage {
    private StyxMessage mAnswer;
    private MessageType mRequiredAnswerType;

    public StyxTMessage(MessageType type, MessageType answer) {
        super(type, StyxMessage.NOTAG);
        mRequiredAnswerType = answer;
    }

    public StyxMessage getAnswer() {
        return mAnswer;
    }

    public void setAnswer(StyxMessage answer) 
            throws StyxException {
        synchronized (this) {
            if (!checkAnswer(answer))
                throw new StyxWrongMessageException(answer, mRequiredAnswerType);
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

    protected boolean checkAnswer(StyxMessage answer) {
        final MessageType received = answer.getType();
        return (mRequiredAnswerType == received || received == MessageType.Rerror);
    }
}
