package com.v2soft.styxlib.library.io;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.v2soft.styxlib.library.types.ULong;

public abstract class StyxDataWriter implements IStyxDataReader {
    private static final int sDataBufferSize = 16;
    private static final Charset sUTFCharset = Charset.forName("utf-8");
    private byte [] mDataBuffer;
    
    public StyxDataWriter() {
        mDataBuffer = new byte[sDataBufferSize];
    }    
    protected abstract long getInteger(int bytes);
    public abstract void clear();
    public abstract void limit(int value);
    public abstract void write(byte[] data);
    public abstract void write(byte[] data, int offset, int count);
    public abstract int read(byte[] data, int offset, int count);

    public short readUInt8() {return (short) (readInteger(1)&0XFF);}
    public int readUInt16() {return (int) (readInteger(2)&0xFFFF);}
    public long readUInt32() {return (readInteger(4) &0xFFFFFFFF);}
    public ULong readUInt64() {
        byte[] bytes = new byte[ULong.ULONG_LENGTH];
        read(bytes, 0, ULong.ULONG_LENGTH);
        return new ULong(bytes);        
    }
    public long getUInt32() {return getInteger(4) & 0xFFFFFFFFL;}
    
    public void writeUInt(long val) {writeInteger(4, val);}
    public void writeUShort(int val) {writeInteger(2, val);}
    public void writeUByte(short val) {writeInteger(1, val);}
    public void writeUInt64(ULong value) {write(value.getBytes());}
    
    public void writeUTF(String string) throws UnsupportedEncodingException {
        byte [] data = string.getBytes(sUTFCharset);
        int count = data.length;
        writeUShort(count);
        write(data);
    }    
    protected void writeInteger(int bytes, long value) {
        for (int i=0; i<bytes; i++) {
            mDataBuffer[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        write(mDataBuffer, 0, bytes);
    }
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
    
    @Override
    public String readUTFString() throws UnsupportedEncodingException {
        int count = readUInt16();
        byte[] bytes = new byte[count];
        read(bytes, 0, count);
        return new String(bytes, sUTFCharset);
    }
}
