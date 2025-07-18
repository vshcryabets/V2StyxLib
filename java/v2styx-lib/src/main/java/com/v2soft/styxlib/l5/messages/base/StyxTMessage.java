package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxInterruptedException;
import com.v2soft.styxlib.exceptions.StyxWrongMessageException;
import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxRErrorMessage;

public class StyxTMessage extends BaseMessage {
    private StyxMessage mAnswer;
    private final int mRequiredAnswerType;

    public StyxTMessage(int type, int answer) {
        super(type, Constants.NOTAG, null);
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

    public synchronized StyxMessage waitForAnswer(long timeout)
            throws StyxException {
        if ( mAnswer == null) try {
            wait(timeout);
        } catch (InterruptedException err) {
            throw new StyxInterruptedException();
        }
        if ( mAnswer == null )
            throw new StyxException("Don't receive answer for "+this.toString());
        if (mAnswer.getType() == MessageType.Rerror) {
            StyxRErrorMessage errorMessage = (StyxRErrorMessage) mAnswer;
            throw StyxErrorMessageException.newInstance(errorMessage.mError);
        }
        return mAnswer;
    }

    protected boolean checkAnswer(StyxMessage answer) {
        final int received = answer.getType();
        return (mRequiredAnswerType == received || received == MessageType.Rerror);
    }
}
