package com.v2soft.styxlib.library.core;

import java.nio.ByteBuffer;

import com.v2soft.styxlib.library.server.StyxBufferOperations;
import com.v2soft.styxlib.library.types.ULong;

public class StyxByteBuffer extends StyxBufferOperations {
	private ByteBuffer mBuffer;
	
	public StyxByteBuffer(ByteBuffer buffer) {
	    super();
		assert buffer != null;
		mBuffer = buffer;
	}

	@Override
	protected long getInteger(int bytes) {
        int position = mBuffer.position();
		long result = readInteger(bytes);
		mBuffer.position(position);
		return result;
	}

	/**
	 * Reset position & limit
	 */
	@Override
	public void clear() {
		mBuffer.position(0);
		mBuffer.limit(mBuffer.capacity());
	}
	@Override
	public void limit(int packetSize) {
		mBuffer.limit(packetSize);
	}

	/**
	 * 
	 * @return Byte buffer
	 */
	public ByteBuffer getBuffer() {
		return mBuffer;
	}

    @Override
    public void write(byte[] data) {
        mBuffer.put(data);
    }

    @Override
    public void write(byte[] data, int offset, int count) {
        mBuffer.put(data, offset, count);
    }

    @Override
    public int read(byte[] mData, int offset, int count) {
        mBuffer.get(mData, offset, count);
        return count;
    }

    /**
     * Seek to specified position in buffer
     * @param offset
     */
    public void setPosition(int offset) {
        mBuffer.position(offset);
    }
}
