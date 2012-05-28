package com.v2soft.styxlib.library.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.v2soft.styxlib.library.types.ULong;

public class StyxDataInputStream 
    extends FilterInputStream 
    implements IStyxDataReader {
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
    public ULong readUInt64() throws IOException {
        byte[] bytes = new byte[ULong.ULONG_LENGTH];
        read(bytes, 0, ULong.ULONG_LENGTH);
        return new ULong(bytes);        
    }

    @Override
    public long getUInt32() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String readUTFString() throws IOException {
        int count = readUInt16();
        byte[] bytes = new byte[count];
        read(bytes, 0, count);
        return new String(bytes, sUTFCharset);        
    }
}
