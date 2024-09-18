package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.serialization.IBufferWritter;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class BufferWritterImpl implements IBufferWritter {
    private static final int sDataBufferSize = 16;
    protected static final Charset sUTFCharset = Charset.forName("utf-8");
    private byte[] mInternalBuffer;
    protected ByteBuffer mBuffer;

    public BufferWritterImpl(ByteBuffer buffer) {
        mInternalBuffer = new byte[sDataBufferSize];
        MetricsAndStats.byteArrayAllocationIo++;
        mBuffer = buffer;
    }

    @Override
    public void writeUInt8(short val) {
        mInternalBuffer[0] = (byte) val;
        write(mInternalBuffer, 0, 1);
    }

    @Override
    public void writeUInt16(int val) {
        mInternalBuffer[0] = (byte) ((byte) val & 0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val >> 8) & 0xFF);
        write(mInternalBuffer, 0, 2);
    }

    @Override
    public void writeUInt32(long val) {
        mInternalBuffer[0] = (byte) ((byte) val & 0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val >> 8) & 0xFF);
        mInternalBuffer[2] = (byte) ((byte) (val >> 16) & 0xFF);
        mInternalBuffer[3] = (byte) ((byte) (val >> 24) & 0xFF);
        write(mInternalBuffer, 0, 4);
    }

    @Override
    public void writeUInt64(long val) {
        mInternalBuffer[0] = (byte) ((byte) val & 0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val >> 8) & 0xFF);
        mInternalBuffer[2] = (byte) ((byte) (val >> 16) & 0xFF);
        mInternalBuffer[3] = (byte) ((byte) (val >> 24) & 0xFF);
        mInternalBuffer[4] = (byte) ((byte) (val >> 32) & 0xFF);
        mInternalBuffer[5] = (byte) ((byte) (val >> 40) & 0xFF);
        mInternalBuffer[6] = (byte) ((byte) (val >> 48) & 0xFF);
        mInternalBuffer[7] = (byte) ((byte) (val >> 56) & 0xFF);
        write(mInternalBuffer, 0, 8);
    }

    @Override
    public void writeUTFString(String string) throws StyxException {
        byte[] data = string.getBytes(sUTFCharset);
        writeUInt16(data.length);
        write(data, 0, data.length);
    }

    @Override
    public int write(byte[] data, int offset, int count) {
        try {
            mBuffer.put(data, offset, count);
        } catch (BufferOverflowException err) {
            System.err.printf("Buffer: c=%d, p=%d, l=%d\n", mBuffer.capacity(),
                    mBuffer.position(), mBuffer.limit());
            System.err.printf("Write: c=%d, o=%d, l=%d\n", count,
                    offset, data.length);
            throw err;
        }
        return count;
    }

    @Override
    public void prepareBuffer(int bufferSize) {
        mBuffer.clear();
        mBuffer.limit(bufferSize);
    }
}
