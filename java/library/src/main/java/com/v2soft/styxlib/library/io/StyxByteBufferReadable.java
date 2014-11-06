package com.v2soft.styxlib.library.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxByteBufferReadable implements IStyxBuffer {
    protected ByteBuffer mBuffer;
    protected int mWritePosition;
    protected int mReadPosition;
    protected int mCapacity; // maximal number of bytes that this buffer can contain
    protected int mStoredBytes; // number of bytes that this buffer contain
    protected int mCurrentLimit;

    public StyxByteBufferReadable(int capacity) {
        mCapacity = capacity;
        mBuffer = ByteBuffer.allocateDirect(mCapacity);
        clear();
    }
    // ==========================================================
    // Public methods
    // ==========================================================
    public int remainsToRead() {
        return mStoredBytes;
    }

    public int readFromChannel(SocketChannel channel) throws IOException {
        int free = updateBufferLimits();
        if ( free <= 0 ) return 0;
        int read = channel.read(mBuffer);
        moveWritePointer(read);
        return read;
    }

    @Override
    public int write(byte[] testBuffer, int offset, int length) {
        int free = updateBufferLimits();
        if ( free <= 0 ) {
            // not enought free space
            return 0;
        }
        if ( length > free ) {
            length = free;
        }
        int part1size = mCapacity-mWritePosition;
        if ( part1size > length ) {
            part1size = length;
        }
        int part2size = length-part1size;
        if ( part1size > 0 ) {
            mBuffer.put(testBuffer, offset, part1size);
            moveWritePointer(part1size);
        }
        if ( part2size > 0 ) {
            // rewind write pointer to start of buffer
            updateBufferLimits();
            mBuffer.put(testBuffer, offset+part1size, part2size);
            moveWritePointer(part2size);
        }
        return length;
    }

    @Override
    public ByteBuffer getBuffer() {
        return mBuffer;
    }

    /**
     * Reset position & limit
     */
    public void clear() {
        mWritePosition = 0;
        mReadPosition = 0;
        mStoredBytes = 0;
        mCurrentLimit = mCapacity;
        mBuffer.position(0);
        mBuffer.limit(mCurrentLimit);
    }
    public void limit(int limit) {
        mCurrentLimit = limit;
        mBuffer.limit(mCurrentLimit);
    }

    private int updateBufferLimits() {
        int free = mCapacity-mStoredBytes;
        if ( free <= 0 ) return 0;
        if ( mWritePosition >= mCapacity ) {
            mWritePosition = 0;
        }
        mBuffer.limit( mWritePosition < mReadPosition ? mReadPosition : mCapacity );
        mBuffer.position(mWritePosition);
        return free;
    }

    protected void moveWritePointer(int read) {
        if ( read > 0 ) {
            mStoredBytes+=read;
            mWritePosition=mBuffer.position();
        }
    }

    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out
     * @param offset
     * @param length
     */
    public int get(byte[] out, int offset, int length) {
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
            mBuffer.get(out, offset, avaiable);
            // read second part
            mBuffer.position(0);
            mBuffer.get(out, offset+avaiable, length-avaiable);
        } else {
            // single block
            mBuffer.get(out, offset, length);
        }
        return length;
    }

    @Override
    public void moveReadPointerBy(int bytes) {
        mReadPosition += bytes;
        mStoredBytes -= bytes;
        while ( mReadPosition > mCapacity ) {
            mReadPosition -= mCapacity;
        }
    }
    // ==========================================================
    // StyxDataReader methods
    // ==========================================================
    /**
     * Read byte array from buffer
     * @param out
     * @param offset
     * @param length
     */
    @Override
    public int read(byte[] out, int offset, int length) {
        if ( out == null ) throw new NullPointerException("Out is null");
        if ( mStoredBytes < length ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        int res = get(out, offset, length);
        moveReadPointerBy(res);
        return res;
    }
}
