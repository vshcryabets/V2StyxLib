package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.l5.io.Buffer;
import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class BufferReaderImpl implements BufferReader {

    protected static final Charset sUTFCharset = Charset.forName("utf-8");
    protected static final int sDataBufferSize = 16;
    protected byte[] mInternalBuffer;
    protected Buffer mBuffer;

    public BufferReaderImpl(Buffer buffer) {
        mInternalBuffer = new byte[getInternalBufferSize()];
        MetricsAndStats.byteArrayAllocationIo++;
        mBuffer = buffer;
    }

    protected long readInteger(int bytes) {
        long result = getInteger(bytes);
        mBuffer.moveReadPointerBy(bytes);
        return result;
    }

    protected long getInteger(int bytes) {
        if (bytes > getInternalBufferSize())
            throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
        long result = 0L;
        int shift = 0;
        int readed = mBuffer.get(mInternalBuffer, 0, bytes);
        if (readed != bytes)
            throw new ArrayIndexOutOfBoundsException("Can't read bytes");
        for (int i = 0; i < bytes; i++) {
            long b = (mInternalBuffer[i] & 0xFF);
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
        MetricsAndStats.byteArrayAllocationIo++;
        read(bytes, 0, count);
        return new String(bytes, sUTFCharset);
    }

    @Override
    public short readUInt8() {
        return (short) (readInteger(1) & 0XFF);
    }

    @Override
    public int readUInt16() {
        return (int) (readInteger(2) & 0xFFFF);
    }

    @Override
    public long readUInt32() {
        return (readInteger(4) & 0xFFFFFFFFL);
    }

    @Override
    public long readUInt64() {
        return readInteger(8);
    }

    @Override
    public int read(byte[] data, int offset, int count) {
        return mBuffer.read(data, offset, count);
    }

    @Override
    public long getUInt32() {
        return getInteger(4) & 0xFFFFFFFFL;
    }

    private int getInternalBufferSize() {
        return sDataBufferSize;
    }
}
