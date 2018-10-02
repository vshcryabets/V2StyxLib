package com.v2soft.styxlib.messages.base;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxWrongMessageException;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.utils.SyncObject;

import java.util.concurrent.TimeoutException;

public class StyxTMessage extends StyxMessage {
    private StyxMessage mAnswer;
    private MessageType mRequiredAnswerType;
    // TODO remove or simplify
    private SyncObject mWaitSyncObject;

    public StyxTMessage(MessageType type, MessageType answer) {
        super(type, StyxMessage.NOTAG);
        mRequiredAnswerType = answer;
    }

    public StyxMessage getAnswer() {
        return mAnswer;
    }

    public void setAnswer(StyxMessage answer)
            throws StyxException {
        if (!checkAnswer(answer))
            throw new StyxWrongMessageException(answer, mRequiredAnswerType);
        mAnswer = answer;
        if (mWaitSyncObject != null) {
            synchronized (mWaitSyncObject) {
                mWaitSyncObject.notifyAll();
            }
        }
    }

    // TODO remove or simplify
    public StyxMessage waitForAnswer() throws InterruptedException, TimeoutException,
            StyxErrorMessageException {
        if ( mAnswer == null) {
            synchronized (mWaitSyncObject) {
                if ( mAnswer == null) {
                    mWaitSyncObject.waitForNotify();
                }
            }
        }
        if ( mAnswer == null )
            throw new TimeoutException("Don't receive answer for "+this.toString());
        if (mAnswer.getType() == MessageType.Rerror) {
            StyxErrorMessageException.checkException(mAnswer);
        }
        return mAnswer;
    }

    protected boolean checkAnswer(StyxMessage answer) {
        final MessageType received = answer.getType();
        return (mRequiredAnswerType == received || received == MessageType.Rerror);
    }

    // TODO we can move sync logic out of this class. For example we can have map in the channel driver.
    public void setSyncObject(SyncObject syncObject) {
        mWaitSyncObject = syncObject;
    }
}
