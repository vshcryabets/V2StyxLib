package com.v2soft.styxlib.library.io;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxByteBufferReadable extends StyxDataReader {
    private IoBuffer mBuffer;

    public StyxByteBufferReadable(IoBuffer buffer) {
        mBuffer = buffer;
    }
    // ==========================================================
    // Public methods
    // ==========================================================
    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out
     * @param offset
     * @param length
     */
    public int get(byte[] out, int offset, int length) {
        if ( out == null ) {
            throw new NullPointerException("Out is null");
        }
        if ( mBuffer.remaining() < length ) {
            throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        }
        int position = mBuffer.position();
        mBuffer.get(out, offset, length);
        mBuffer.position(position);
        return length;
    }    
    /**
     * Read byte array from buffer
     * @param out
     * @param offset
     * @param length
     */
    @Override
    public int read(byte[] out, int offset, int length) {
        if ( out == null ) {
            throw new NullPointerException("Out is null");
        }
        if ( mBuffer.remaining() < length ) {
            throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        }
        mBuffer.get(out, offset, length);
        return length;
    }

    @Override
    protected long getInteger(int bytes) {
        // TODO this method will work wrong at the buffer end
        if ( bytes > sDataBufferSize ) {
            throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        }
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
