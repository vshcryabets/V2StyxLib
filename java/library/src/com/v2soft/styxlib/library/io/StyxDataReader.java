package com.v2soft.styxlib.library.io;

import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.types.ULong;

public class StyxDataReader implements IStyxDataReader {
    protected static final int sDataBufferSize = 16;
    protected byte [] mInternalBuffer;
    protected IStyxBuffer mBuffer;

    public StyxDataReader(IStyxBuffer buffer) {
        mInternalBuffer = new byte[getInternalBufferSize()];
        mBuffer = buffer;
    }    

    protected long readInteger(int bytes) {
        long result = getInteger(bytes);
        mBuffer.moveReadPointerBy(bytes);
        return result;
    }
    protected long getInteger(int bytes) {
        if ( bytes > getInternalBufferSize() ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        long result = 0L;
        int shift = 0;
        int readed = mBuffer.get(mInternalBuffer, 0, bytes);
        if ( readed != bytes ) throw new ArrayIndexOutOfBoundsException("Can't read bytes");
        for (int i=0; i<bytes; i++) {
            long b = (mInternalBuffer[i]&0xFF);
            if (shift > 0)
                b <<= shift;
            shift += 8;
            result |= b;
        }
        return result;
    }
    // ================================================================
    // IStyxDataReader methods
    // ================================================================
    @Override
    public String readUTFString() throws UnsupportedEncodingException {
        int count = readUInt16();
        byte[] bytes = new byte[count];
        read(bytes, 0, count);
        return new String(bytes, StyxDataWriter.sUTFCharset);
    }
    @Override
    public short readUInt8() {return (short) (readInteger(1)&0XFF);}
    @Override
    public int readUInt16() {return (int) (readInteger(2)&0xFFFF);}
    @Override
    public long readUInt32() {return (readInteger(4) &0xFFFFFFFF);}
    @Override
    public ULong readUInt64() {
        byte[] bytes = new byte[ULong.ULONG_LENGTH];
        read(bytes, 0, ULong.ULONG_LENGTH);
        return new ULong(bytes);        
    }
    @Override
    public int read(byte[] data, int offset, int count) {
        return mBuffer.read(data, offset, count);
    }
    @Override
    public long getUInt32() {return getInteger(4) & 0xFFFFFFFFL;}

    public int getInternalBufferSize() {
        return sDataBufferSize;
    }
}
