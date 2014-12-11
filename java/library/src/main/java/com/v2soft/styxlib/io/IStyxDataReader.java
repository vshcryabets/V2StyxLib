package com.v2soft.styxlib.io;

import com.v2soft.styxlib.library.types.ULong;

import java.io.IOException;

public interface IStyxDataReader {
    short readUInt8() throws IOException;
    int readUInt16() throws IOException;
    long readUInt32() throws IOException;
    ULong readUInt64() throws IOException;
    long getUInt32();
    String readUTFString() throws IOException;
    int read(byte[] data, int offset, int dataLength) throws IOException;
}
