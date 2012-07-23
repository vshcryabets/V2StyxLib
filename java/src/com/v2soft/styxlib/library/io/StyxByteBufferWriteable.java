package com.v2soft.styxlib.library.io;

import org.apache.mina.core.buffer.IoBuffer;

public class StyxByteBufferWriteable extends StyxDataWriter {
	private IoBuffer mBuffer;
	
	public StyxByteBufferWriteable(int capacity) {
	    super();
	    mBuffer = IoBuffer.allocate(capacity);
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
	public IoBuffer getBuffer() {
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
