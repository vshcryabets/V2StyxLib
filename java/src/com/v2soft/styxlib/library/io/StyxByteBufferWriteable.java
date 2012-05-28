package com.v2soft.styxlib.library.io;

import java.nio.ByteBuffer;

public class StyxByteBufferWriteable extends StyxDataWriter {
	private ByteBuffer mBuffer;
	
	public StyxByteBufferWriteable(int capacity) {
	    super();
	    mBuffer = ByteBuffer.allocateDirect(capacity);
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

    /**
     * Seek to specified position in buffer
     * @param offset
     */
    public void setPosition(int offset) {
        mBuffer.position(offset);
    }

    @Override
    public int write(byte[] data, int offset, int count) {
        mBuffer.put(data, offset, count);
        return count;
    }
}
