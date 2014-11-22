package com.v2soft.styxlib.library.utils;

import com.v2soft.styxlib.library.messages.base.StyxMessage;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class FIDPoll extends AbstractPoll<Long> {
    public static final long MAXUNINT = 0xFFFFFFFFL;
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
