package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.v2soft.styxlib.library.types.ULong;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class DualStateBuffer {
    private static final int sDataBufferSize = 16; 
    private byte [] mDataBuffer;
    private ByteBuffer mBuffer;
    private int mWritePosition, mReadPosition, mCapacity, mStoredBytes;

    public DualStateBuffer(int capacity) {
        mWritePosition = 0;
        mReadPosition = 0;
        mStoredBytes = 0;
        mCapacity = capacity;
        mBuffer = ByteBuffer.allocateDirect(mCapacity);
        mDataBuffer = new byte[sDataBufferSize];

    }

    public int remainsToRead() {
        return mStoredBytes;
    }

    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out
     * @param i
     * @param length
     */
    public int get(byte[] out, int i, int length) {
        assert out != null;
        assert mStoredBytes > length;
        if ( mReadPosition >= mCapacity ) {
            mReadPosition = 0;
        }
        mBuffer.position(mReadPosition);
        mBuffer.limit( mWritePosition <= mReadPosition ? mCapacity : mWritePosition );
        mBuffer.get(out, i, length);
        return length;
    }
    
    /**
     * Read byte array from buffer
     * @param out
     * @param i
     * @param length
     */
    public int read(byte[] out, int i, int length) {
        assert out != null;
        assert mStoredBytes > length;
        int res = get(out, i, length);
        mReadPosition=mBuffer.position();
        mStoredBytes-=res;
        return res;
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

    private long getInteger(int bytes) {
        assert bytes < sDataBufferSize;
        long result = 0L;
        int shift = 0;
        int readed = get(mDataBuffer, 0, bytes);
        assert readed == bytes;
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

    private long readInteger(int bytes) {
        assert bytes < sDataBufferSize;
        long result = getInteger(bytes);
        mReadPosition+=bytes;
        mStoredBytes-=bytes;
        return result;
    }
    
    public long readUInt32() {
        return readInteger(4);
    }

    public int readUInt16() {
        return (int) readInteger(2);
    }

    public short readUInt8() {
        return (short) (readInteger(1)&0XFF);
    }
    public ULong readUInt64() {
        byte[] bytes = new byte[ULong.ULONG_LENGTH];
        read(bytes, 0, ULong.ULONG_LENGTH);
        return new ULong(bytes);
    }

    public long getUInt32() {
        return getInteger(4) & 0xFFFFFFFFL;
    }

    public String readUTF() throws UnsupportedEncodingException {
        int count = readUInt16();
        byte[] bytes = new byte[count];
        read(bytes, 0, count);
        return new String(bytes, "UTF-8");
    }


}
