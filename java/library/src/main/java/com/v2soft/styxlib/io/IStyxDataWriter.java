package com.v2soft.styxlib.io;

import com.v2soft.styxlib.library.types.ULong;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface IStyxDataWriter {
    void writeUInt8(short val) throws IOException;
    void writeUInt16(int val) throws IOException;
    void writeUInt32(long val) throws IOException;
    void writeUInt64(ULong value) throws IOException;
    void writeUTFString(String string) throws UnsupportedEncodingException, IOException;
    int write(byte[] data, int offset, int count);
    void clear();
    void limit(int limit);
    java.nio.ByteBuffer getBuffer();
}
