package com.v2soft.styxlib.io;

import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StyxDataInputStream
    extends FilterInputStream
    implements BufferReader {
    // ==================================================
    // Constants
    // ==================================================
    private static final int sDataBufferSize = 16;
    private static final Charset sUTFCharset = Charset.forName("utf-8");
    // ==================================================
    // Class fields
    // ==================================================
    private byte [] mDataBuffer;

    public StyxDataInputStream(InputStream in) {
        super(in);
        mDataBuffer = new byte[sDataBufferSize];
        MetricsAndStats.byteArrayAllocationIo++;
    }


    private long readInteger(int bytes) throws IOException {
        if ( bytes > sDataBufferSize ) throw new IllegalArgumentException("Too much bytes to read");
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
    // ==================================================
    // IStyxDataReader
    // ==================================================
    @Override
    public short readUInt8() throws IOException {return (short) (readInteger(1)&0XFF);}
    @Override
    public int readUInt16() throws IOException {return (int) (readInteger(2)&0xFFFF);}
    @Override
    public long readUInt32() throws IOException {return (readInteger(4) &0xFFFFFFFF);}
    @Override
    public long readUInt64() throws IOException {
        return readInteger(8);
    }

    @Override
    public long getUInt32() {
        throw  new IllegalStateException("Not implemented");
    }

    @Override
    public String readUTFString() throws IOException {
        int count = readUInt16();
        byte[] bytes = new byte[count];
        MetricsAndStats.byteArrayAllocationIo++;
        read(bytes, 0, count);
        return new String(bytes, "utf-8");
    }
}
