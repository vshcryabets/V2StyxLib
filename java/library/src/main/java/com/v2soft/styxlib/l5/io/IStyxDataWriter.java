package com.v2soft.styxlib.l5.io;

import java.io.IOException;

public interface IStyxDataWriter {
    void writeUInt8(short val) throws IOException;
    void writeUInt16(int val) throws IOException;
    void writeUInt32(long val) throws IOException;
    void writeUInt64(long value) throws IOException;
    void writeUTFString(String string) throws IOException;
    int write(byte[] data, int offset, int count);
    void clear();
    void limit(int limit);
    java.nio.ByteBuffer getBuffer();
}
