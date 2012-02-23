package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class DualStateBuffer {
    private ByteBuffer mBuffer;
    private int mWritePosition, mReadPosition, mCapacity, mStoredBytes;

    public DualStateBuffer(int capacity) {
        mWritePosition = 0;
        mReadPosition = 0;
        mStoredBytes = 0;
        mCapacity = capacity;
        mBuffer = ByteBuffer.allocateDirect(mCapacity);
    }

    public int remainsToRead() {
        return mStoredBytes;
    }

    /**
     * Read byte array from buffer
     * @param out
     * @param i
     * @param length
     */
    public void read(byte[] out, int i, int length) {
        assert out != null;
        assert mStoredBytes > length;
        if ( mReadPosition >= mCapacity ) {
            mReadPosition = 0;
        }
        mBuffer.position(mReadPosition);
        mBuffer.limit( mWritePosition <= mReadPosition ? mCapacity : mWritePosition );
        mBuffer.get(out, i, length);
        mReadPosition=mBuffer.position();
        mStoredBytes-=length;
    }

    public int readFromChannel(SocketChannel channel) throws IOException {
        int free = mCapacity-mStoredBytes;
        if ( free <= 0 ) return 0;
        if ( mWritePosition >= mCapacity ) {
            mWritePosition = 0;
        }
        mBuffer.limit( mWritePosition < mReadPosition ? mReadPosition : mCapacity );
        mBuffer.position(mWritePosition);
        int readed = channel.read(mBuffer);
        if ( readed > 0 ) {
            mStoredBytes+=readed;
            mWritePosition=mBuffer.position();
        }
        return readed;
    }
}
