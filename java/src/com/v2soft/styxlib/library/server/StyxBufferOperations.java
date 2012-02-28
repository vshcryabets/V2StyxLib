package com.v2soft.styxlib.library.server;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.v2soft.styxlib.library.types.ULong;

public abstract class StyxBufferOperations {
    private static final int sDataBufferSize = 16; 
    private byte [] mDataBuffer;
    
    public StyxBufferOperations() {
        mDataBuffer = new byte[sDataBufferSize];
    }    
    protected abstract long getInteger(int bytes);
    protected abstract long readInteger(int bytes);
    public abstract String readUTF() throws UnsupportedEncodingException;
    public abstract ULong readUInt64();
    public abstract void clear();
    public abstract void limit(int value);
    public abstract void write(byte[] data);
    public abstract void write(byte[] data, int offset, int count);

    public long readUInt32() {
        return (readInteger(4) &0xFFFFFFFF);
    }

    public int readUInt16() {
        return (int) (readInteger(2)&0xFFFF);
    }

    public short readUInt8() {
        return (short) (readInteger(1)&0XFF);
    }

    public long getUInt32() {
        return getInteger(4) & 0xFFFFFFFFL;
    }
    public void writeUInt(long val) {
        writeInteger(4, val);
    }
    public void writeUByte(short val) {
        writeInteger(1, val);
    }
    public void writeUShort(int val) {
        writeInteger(2, val);
    }
    public void writeUInt64(ULong value) {
        write(value.getBytes());
    }
    public void writeUTF(String string) throws UnsupportedEncodingException {
        byte [] data = string.getBytes("UTF-8"); // TODO Use charset object
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
}
