package com.v2soft.styxlib.library.types;

import java.util.LinkedList;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 * @param <T>
 */
public class ObjectsPoll<T> {
    public interface ObjectsPollFactory<T> {
        T create();
    };
    
    private ObjectsPollFactory<T> mFactory;
    private LinkedList<T> mAvailableObjects;
    
    public ObjectsPoll(ObjectsPollFactory<T> factory) {
        mFactory = factory;
        mAvailableObjects = new LinkedList<T>();
    }
    
    public T get() {
        synchronized (mAvailableObjects) {
            if (!mAvailableObjects.isEmpty())
                return mAvailableObjects.poll();
            return mFactory.create();
        }
    }
    
    public boolean release(T object) {
        synchronized (mAvailableObjects) {
            assert ! mAvailableObjects.contains(object);
            return mAvailableObjects.add(object);
        }        
    }

}
