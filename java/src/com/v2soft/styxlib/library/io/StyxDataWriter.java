package com.v2soft.styxlib.library.io;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.v2soft.styxlib.library.types.ULong;

public abstract class StyxDataWriter implements IStyxDataWriter {
    private static final int sDataBufferSize = 16;
    private static final Charset sUTFCharset = Charset.forName("utf-8");
    private byte [] mDataBuffer;
    
    public StyxDataWriter() {
        mDataBuffer = new byte[sDataBufferSize];
    }    
    @Override
    public abstract void clear();
    @Override
    public abstract void limit(int value);
    @Override
    public abstract int write(byte[] data, int offset, int count);
    public abstract int read(byte[] data, int offset, int count);

    public void write(byte[] data) {
        write(data, 0, data.length);
    }
    public void writeUInt8(short val) {writeInteger(1, val);}
    @Override
    public void writeUInt16(int val) {writeInteger(2, val);}
    @Override
    public void writeUInt32(long val) {writeInteger(4, val);}
    @Override
    public void writeUInt64(ULong value) {write(value.getBytes());}
    @Override
    public void writeUTFString(String string) throws UnsupportedEncodingException {
        byte [] data = string.getBytes(sUTFCharset);
        int count = data.length;
        writeUInt16(count);
        write(data);
    }    
    protected void writeInteger(int bytes, long value) {
        for (int i=0; i<bytes; i++) {
            mDataBuffer[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        write(mDataBuffer, 0, bytes);
    }
}
