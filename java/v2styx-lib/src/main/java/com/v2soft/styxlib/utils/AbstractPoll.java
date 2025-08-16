package com.v2soft.styxlib.utils;

import java.util.HashSet;

/**
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public abstract class AbstractPoll<T> {
    protected HashSet<T> mAvailable = new HashSet<T>();
    protected T mLast = null;

    /**
     * @return Return free item
     */
    public T getFreeItem() {
        synchronized (mAvailable) {
            if (!mAvailable.isEmpty()) {
                T result = mAvailable.iterator().next();
                mAvailable.remove(result);
                return result;
            }
            mLast = getNext();
            return mLast;
        }
    }

    protected abstract T getNext();

    public boolean release(T id) {
        synchronized (mAvailable) {
            if ( mAvailable.contains(id)) {
                throw new IllegalStateException(
                        String.format("Something goes wrong, this item (%s) already has been released",
                                String.valueOf(id)));
            }
            return mAvailable.add(id);
        }
    }
    public void clean() {
        mAvailable.clear();
        mLast = null;
    }
}
