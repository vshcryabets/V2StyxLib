package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxWrongMessageException;
import com.v2soft.styxlib.l5.enums.MessageType;

import java.util.concurrent.TimeoutException;

public class StyxTMessage extends StyxMessage {
    private StyxMessage mAnswer;
    private final MessageType mRequiredAnswerType;

    public StyxTMessage(MessageType type, MessageType answer) {
        super(type, NOTAG);
        mRequiredAnswerType = answer;
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

    public synchronized StyxMessage waitForAnswer(long timeout) throws InterruptedException, TimeoutException,
            StyxErrorMessageException {
        if ( mAnswer == null)
            wait(timeout);
        if ( mAnswer == null )
            throw new TimeoutException("Don't receive answer for "+this.toString());
        if (mAnswer.getType() == MessageType.Rerror) {
            StyxErrorMessageException.doException(mAnswer, null);
        }
        return mAnswer;
    }

    protected boolean checkAnswer(StyxMessage answer) {
        final MessageType received = answer.getType();
        return (mRequiredAnswerType == received || received == MessageType.Rerror);
    }
}
