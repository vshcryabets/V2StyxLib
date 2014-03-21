package com.v2soft.styxlib.library.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxByteBufferReadable extends StyxDataReader {
    private ByteBuffer mBuffer;
    private int mWritePosition, mReadPosition, mCapacity, mStoredBytes;

    public StyxByteBufferReadable(int capacity) {
        mWritePosition = 0;
        mReadPosition = 0;
        mStoredBytes = 0;
        mCapacity = capacity;
        mBuffer = ByteBuffer.allocateDirect(mCapacity);
    }
    // ==========================================================
    // Public methods
    // ==========================================================
    public int remainsToRead() {
        return mStoredBytes;
    }
    public int readFromChannel(SocketChannel channel) throws IOException {
        int free = mCapacity-mStoredBytes;
        if ( free <= 0 ) return 0;
        if ( mWritePosition >= mCapacity ) {
            mWritePosition = 0;
        }
        mBuffer.limit( mWritePosition < mReadPosition ? mReadPosition : mCapacity );
        mBuffer.position(mWritePosition);
        int read = channel.read(mBuffer);
        if ( read > 0 ) {
            mStoredBytes+=read;
            mWritePosition=mBuffer.position();
        }
        return read;
    }
    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out
     * @param i
     * @param length
     */
    public int get(byte[] out, int i, int length) {
        if ( out == null ) throw new NullPointerException("Out buffer is null");
        if ( mStoredBytes < length ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        if ( mReadPosition >= mCapacity ) {
            mReadPosition = 0;
        }
        mBuffer.position(mReadPosition);
        int limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
        mBuffer.limit( limit );
        int avaiable = limit-mReadPosition;
        if ( avaiable < length ) {
            // splited block
            // read first part
            mBuffer.get(out, i, avaiable);
            // read second part
            mBuffer.position(0);
            mBuffer.get(out, i+avaiable, length-avaiable);
        } else {
            // single block
            mBuffer.get(out, i, length);
        }
        return length;
    }    
    // ==========================================================
    // StyxDataReader methods
    // ==========================================================
    /**
     * Read byte array from buffer
     * @param out
     * @param i
     * @param length
     */
    @Override
    public int read(byte[] out, int i, int length) {
        if ( out == null ) throw new NullPointerException("Out is null");
        if ( mStoredBytes < length ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        int res = get(out, i, length);
        mReadPosition=mBuffer.position();
        mStoredBytes-=res;
        return res;
    }

    @Override
    protected long getInteger(int bytes) {
        // TODO this method will work wrong at the buffer end
        if ( bytes > sDataBufferSize ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        long result = 0L;
        int shift = 0;
        int readed = get(mDataBuffer, 0, bytes);
        if ( readed != bytes ) throw new ArrayIndexOutOfBoundsException("Can't read bytes");
        for (int i=0; i<bytes; i++) {
            long b = (mDataBuffer[i]&0xFF);
            if (shift > 0)
                b <<= shift;
            shift += 8;         
            result |= b;
        }       
        return result;
    }
}
