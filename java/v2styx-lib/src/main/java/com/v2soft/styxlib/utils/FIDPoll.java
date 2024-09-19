package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.Logger;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class FIDPoll extends AbstractPoll<Long> {
    public static final long MAXUNINT = 0xFFFFFFFFL;

    public FIDPoll() {
        mLast = Long.valueOf(1);
    }

    @Override
    public Long getFreeItem() {
        var id = super.getFreeItem();
        if (Config.DEBUG_FID_POLL)
            Logger.DEBUG.println("Reserve " + id);
        return id;
    }

    @Override
    public boolean release(Long id) {
        if (id == StyxMessage.NOFID)
            return false;
        if (Config.DEBUG_FID_POLL)
            Logger.DEBUG.println("Release " + id);
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
