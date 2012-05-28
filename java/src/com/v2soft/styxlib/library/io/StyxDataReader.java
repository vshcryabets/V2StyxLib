package com.v2soft.styxlib.library.io;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.v2soft.styxlib.library.types.ULong;

public abstract class StyxDataReader implements IStyxDataReader {
    protected static final int sDataBufferSize = 16;
    private static final Charset sUTFCharset = Charset.forName("utf-8");
    protected byte [] mDataBuffer;

    public StyxDataReader() {
        mDataBuffer = new byte[sDataBufferSize];
    }    
    protected abstract long getInteger(int bytes);

    protected long readInteger(int bytes) {
        long result = 0L;
        int shift = 0;
        read(mDataBuffer, 0, bytes);
        for (int i=0; i<bytes; i++) {
            long b = (mDataBuffer[i]&0xFF);
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
        return new String(bytes, sUTFCharset);
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
    public abstract int read(byte[] data, int offset, int count);
    @Override
    public long getUInt32() {return getInteger(4) & 0xFFFFFFFFL;}
}
