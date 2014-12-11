package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.messages.base.StyxMessage;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class FIDPoll extends AbstractPoll<Long> {
    public static final long MAXUNINT = 0xFFFFFFFFL;

    public FIDPoll() {
        mLast = Long.valueOf(1);
    }

    @Override
    public boolean release(Long id) {
        if (id == StyxMessage.NOFID)
            return false;
        return super.release(id);
    }
    @Override
    protected Long getNext() {
        mLast++;
        if(mLast > MAXUNINT)
            mLast = 0L;
        return mLast;
    }
    @Override
    public void clean() {
        mLast = 0L;
    }
}
