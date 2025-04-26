package com.v2soft.styxlib.io;

import com.v2soft.styxlib.exceptions.StyxEOFException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StyxDataInputStream
    extends FilterInputStream
    implements IBufferReader {
    private static final int sDataBufferSize = 16;
    private final byte [] mDataBuffer;

    public StyxDataInputStream(InputStream in) {
        super(in);
        mDataBuffer = new byte[sDataBufferSize];
        MetricsAndStats.byteArrayAllocationIo++;
    }

    private long readInteger(int bytes) throws StyxException {
        if ( bytes > sDataBufferSize ) throw new IllegalArgumentException("Too much bytes to read");
        long result = 0L;
        int shift = 0;
        var read = readData(mDataBuffer, 0, bytes);
        if (read <= 0 ) {
            throw new StyxEOFException();
        }
        if (read < bytes) {
            throw new StyxException("Can't read " + bytes + " bytes from stream");
        }
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
    // IBufferReader
    // ==================================================
    @Override
    public short readUInt8() throws StyxException {return (short) (readInteger(1)&0XFF);}
    @Override
    public int readUInt16() throws StyxException {return (int) (readInteger(2)&0xFFFF);}
    @Override
    public long readUInt32() throws StyxException {return (readInteger(4));}
    @Override
    public long readUInt64() throws StyxException {return readInteger(8);}

    @Override
    public long getUInt32() {
        throw  new IllegalStateException("Not implemented");
    }

    @Override
    public String readUTFString() throws StyxException {
        int count = readUInt16();
        byte[] bytes = new byte[count];
        MetricsAndStats.byteArrayAllocationIo++;
        readData(bytes, 0, count);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public int readData(byte[] data, int offset, int dataLength) throws StyxException {
        try {
            return super.read(data, offset, dataLength);
        } catch (IOException e) {
            throw new StyxException(e.toString());
        }
    }
}
