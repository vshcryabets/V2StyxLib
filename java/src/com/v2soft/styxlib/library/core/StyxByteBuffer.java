package com.v2soft.styxlib.library.core;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


import com.v2soft.styxlib.library.server.StyxBufferOperations;
import com.v2soft.styxlib.library.types.ULong;

public class StyxByteBuffer extends StyxBufferOperations {
    private static final int sDataBufferSize = 16; 
    private byte [] mDataBuffer;
	private ByteBuffer mBuffer;
	
	public StyxByteBuffer(ByteBuffer buffer) {
		assert buffer != null;
		mBuffer = buffer;
		mDataBuffer = new byte[sDataBufferSize];
	}

	@Override
	protected long getInteger(int bytes) {
        int position = mBuffer.position();
		long result = readInteger(bytes);
		mBuffer.position(position);
		return result;
	}

	@Override
	protected long readInteger(int bytes) {
        assert bytes < mBuffer.remaining();
        long result = 0L;
        int shift = 0;
        mBuffer.get(mDataBuffer, 0, bytes);
        for (int i=0; i<bytes; i++)
        {
            long b = (mDataBuffer[i]&0xFF);
            if (shift > 0)
                b <<= shift;
            shift += 8;         
            result |= b;
        }       
        return result;		
	}

	@Override
	public String readUTF() throws UnsupportedEncodingException {
    	int count = readUInt16();
        byte[] bytes = new byte[count];
        mBuffer.get(bytes, 0, count);
        return new String(bytes, "UTF-8");
	}

	@Override
	public ULong readUInt64() {
		assert 8 < mBuffer.remaining();
        byte[] bytes = new byte[ULong.ULONG_LENGTH];
        mBuffer.get(bytes, 0, ULong.ULONG_LENGTH);
        return new ULong(bytes);		
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
}
