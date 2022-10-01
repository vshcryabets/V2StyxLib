package com.v2soft.styxlib.l5.io;

import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.UnsupportedEncodingException;

public class StyxDataReader implements IStyxDataReader {
    protected static final int sDataBufferSize = 16;
    protected byte [] mInternalBuffer;
    protected IStyxBuffer mBuffer;

    public StyxDataReader(IStyxBuffer buffer) {
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
        MetricsAndStats.byteArrayAllocationIo++;
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
    public long readUInt64() {
        return readInteger(8);
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
