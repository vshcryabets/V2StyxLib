package com.v2soft.styxlib.library.io;

import java.nio.ByteBuffer;

public class StyxByteBufferReadable extends StyxDataReader {
	private ByteBuffer mBuffer;
	
	public StyxByteBufferReadable(ByteBuffer buffer) {
	    super();
	    if ( buffer == null ) throw new NullPointerException("Buffer is null");
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
