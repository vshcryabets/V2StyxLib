package com.v2soft.styxlib.l5.io.impl;

import com.v2soft.styxlib.l5.io.Buffer;
import com.v2soft.styxlib.l5.io.BufferLoader;
import com.v2soft.styxlib.l5.io.InChannel;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class BufferImpl implements Buffer, BufferLoader {
    protected ByteBuffer mBuffer;
    protected int mWritePosition;
    protected int mReadPosition;
    protected int mCapacity; // maximal number of bytes that this buffer can contain
    protected int mStoredBytes; // number of bytes that this buffer contain
    protected int mCurrentLimit;

    public BufferImpl(int capacity) {
        mCapacity = capacity;
        mBuffer = ByteBuffer.allocateDirect(mCapacity);
        MetricsAndStats.byteBufferAllocation++;
        clear();
    }

    @Override
    public int remainsToRead() {
        return mStoredBytes;
    }

    @Override
    public int write(byte[] testBuffer, int offset, int length) {
        int free = updateBufferLimits();
        if (free <= 0) {
            // not enougth free space in buffer
            return 0;
        }
        if (length > free) {
            length = free;
        }
        int part1size = mCapacity - mWritePosition;
        if (part1size > length) {
            part1size = length;
        }
        int part2size = length - part1size;
        if (part1size > 0) {
            mBuffer.put(testBuffer, offset, part1size);
            moveWritePointer(part1size);
        }
        if (part2size > 0) {
            // rewind write pointer to start of buffer
            updateBufferLimits();
            mBuffer.put(testBuffer, offset + part1size, part2size);
            moveWritePointer(part2size);
        }
        return length;
    }

    private void clear() {
        mWritePosition = 0;
        mReadPosition = 0;
        mStoredBytes = 0;
        mCurrentLimit = mCapacity;
        mBuffer.position(0);
        mBuffer.limit(mCurrentLimit);
    }

    protected int updateBufferLimits() {
        int free = mCapacity - mStoredBytes;
        if (free <= 0) return 0;
        if (mWritePosition >= mCapacity) {
            mWritePosition = 0;
        }
        mBuffer.limit(mWritePosition < mReadPosition ? mReadPosition : mCapacity);
        mBuffer.position(mWritePosition);
        return free;
    }

    protected void moveWritePointer(int read) {
        if (read > 0) {
            mStoredBytes += read;
            mWritePosition = mBuffer.position();
        }
    }

    @Override
    public int get(byte[] out, int offset, int length) {
        if (out == null) throw new NullPointerException("Out buffer is null");
        if (mStoredBytes < length) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        if (mReadPosition >= mCapacity) {
            mReadPosition = 0;
        }
        mBuffer.position(mReadPosition);
        int limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
        mBuffer.limit(limit);
        int avaiable = limit - mReadPosition;
        if (avaiable < length) {
            // splited block
            // read first part
            mBuffer.get(out, offset, avaiable);
            // read second part
            mBuffer.position(0);
            mBuffer.get(out, offset + avaiable, length - avaiable);
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
        while (mReadPosition > mCapacity) {
            mReadPosition -= mCapacity;
        }
    }

    @Override
    public int read(byte[] out, int offset, int length) throws ArrayIndexOutOfBoundsException, NullPointerException {
        if (out == null) throw new NullPointerException("Out is null");
        if (mStoredBytes < length) throw new ArrayIndexOutOfBoundsException("Too much bytes to read "
                + mStoredBytes + " < " + length);
        int res = get(out, offset, length);
        moveReadPointerBy(res);
        return res;
    }

    @Override
    public int readFromChannelToBuffer(InChannel channel) throws IOException {
        int free = updateBufferLimits();
        if (free <= 0) return 0;
        int read = channel.read(mBuffer);
        moveWritePointer(read);
        return read;
    }
}
