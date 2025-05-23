package com.v2soft.styxlib.utils;

import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class MessageTagPoll extends AbstractPoll<Integer> {
    public static final int MAXUSHORT = 0xFFFF;

    @Override
    protected Integer getNext() {
        if ( mLast == null ) {
            mLast = 0;
        }
        mLast++;
        if (mLast > MAXUSHORT)
            mLast = 0;
        return mLast;
    }

    @Override
    public boolean release(Integer id) {
        if (id == Constants.NOTAG)
            return false;
        return super.release(id);
    }
}
