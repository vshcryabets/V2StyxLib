package com.v2soft.styxlib.library.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.v2soft.styxlib.library.types.ULong;

public class StyxDataWriter implements IStyxDataWriter {
    private static final int sDataBufferSize = 16;
    protected static final Charset sUTFCharset = Charset.forName("utf-8");
    private byte [] mInternalBuffer;
    protected ByteBuffer mBuffer;

    public StyxDataWriter(ByteBuffer buffer) {
        mInternalBuffer = new byte[sDataBufferSize];
        mBuffer = buffer;
    }    

    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    @Override
    public void writeUInt8(short val) {
        mInternalBuffer[0] = (byte) val;
        write(mInternalBuffer, 0, 1);
    }
    @Override
    public void writeUInt16(int val) {
        mInternalBuffer[0] = (byte) ((byte) val&0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val>>8)&0xFF);
        write(mInternalBuffer, 0, 2);
    }
    @Override
    public void writeUInt32(long val) {
        mInternalBuffer[0] = (byte) ((byte) val&0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val>>8)&0xFF);
        mInternalBuffer[2] = (byte) ((byte) (val>>16)&0xFF);
        mInternalBuffer[3] = (byte) ((byte) (val>>24)&0xFF);
        write(mInternalBuffer, 0, 4);
    }
    @Override
    public void writeUInt64(ULong value) {
        write(value.getBytes());
    }
    @Override
    public void writeUTFString(String string) throws UnsupportedEncodingException {
        byte [] data = string.getBytes(sUTFCharset);
        writeUInt16(data.length);
        write(data);
    }

    @Override
    public int write(byte[] data, int offset, int count) {
        mBuffer.put(data, offset, count);
        return count;
    }

    @Override
    public void clear() {
        mBuffer.clear();
    }

    @Override
    public void limit(int limit) {
        mBuffer.limit(limit);
    }

    @Override
    public ByteBuffer getBuffer() {
        return mBuffer;
    }
}
