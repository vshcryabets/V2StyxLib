package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.serialization.IBufferWriter;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BufferWriterImpl implements IBufferWriter {
    private static final int sDataBufferSize = 16;
    protected static final Charset sUTFCharset = StandardCharsets.UTF_8;
    private final byte[] mInternalBuffer;
    private final ByteBuffer mBuffer;

    public BufferWriterImpl(int size) {
        mInternalBuffer = new byte[sDataBufferSize];
        MetricsAndStats.byteArrayAllocationIo++;
        mBuffer = ByteBuffer.allocate(size);
        MetricsAndStats.byteBufferAllocation++;
    }

    @Override
    public void writeUInt8(short val) throws StyxException {
        mInternalBuffer[0] = (byte) val;
        write(mInternalBuffer, 0, 1);
    }

    @Override
    public void writeUInt16(int val) throws StyxException {
        mInternalBuffer[0] = (byte) ((byte) val & 0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val >> 8) & 0xFF);
        write(mInternalBuffer, 0, 2);
    }

    @Override
    public void writeUInt32(long val) throws StyxException {
        mInternalBuffer[0] = (byte) ((byte) val & 0xFF);
        mInternalBuffer[1] = (byte) ((byte) (val >> 8) & 0xFF);
        mInternalBuffer[2] = (byte) ((byte) (val >> 16) & 0xFF);
        mInternalBuffer[3] = (byte) ((byte) (val >> 24) & 0xFF);
        write(mInternalBuffer, 0, 4);
    }

    @Override
    public void writeUInt64(long val) throws StyxException {
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
    public int write(byte[] data, int offset, int count) throws StyxException {
        try {
            mBuffer.put(data, offset, count);
        } catch (BufferOverflowException err) {
            String message = String.format(
                    "Buffer overflow: Buffer: c=%d, p=%d, l=%d, Write: c=%d, o=%d, l=%d",
                    mBuffer.capacity(), mBuffer.position(), mBuffer.limit(),
                    count, offset, data.length);
            throw new com.v2soft.styxlib.l5.serialization.BufferOverflowException(message);
        }
        return count;
    }

    @Override
    public void prepareBuffer(int bufferSize) {
        mBuffer.clear();
        mBuffer.limit(bufferSize);
    }

    @Override
    public ByteBuffer getBuffer() {
        return mBuffer;
    }

    @Override
    public int getPosition() {
        return mBuffer.position();
    }

    @Override
    public int getLimit() {
        return mBuffer.limit();
    }
}
