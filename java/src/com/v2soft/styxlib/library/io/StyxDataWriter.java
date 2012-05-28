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

    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    @Override
    public void writeUInt8(short val) {
        mDataBuffer[0] = (byte) val;
        write(mDataBuffer, 0, 1);
    }
    @Override
    public void writeUInt16(int val) {
        mDataBuffer[0] = (byte) ((byte) val&0xFF);
        mDataBuffer[1] = (byte) ((byte) (val>>8)&0xFF);
        write(mDataBuffer, 0, 2);
    }
    @Override
    public void writeUInt32(long val) {
        mDataBuffer[0] = (byte) ((byte) val&0xFF);
        mDataBuffer[1] = (byte) ((byte) (val>>8)&0xFF);
        mDataBuffer[2] = (byte) ((byte) (val>>16)&0xFF);
        mDataBuffer[3] = (byte) ((byte) (val>>24)&0xFF);
        write(mDataBuffer, 0, 4);
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
}
