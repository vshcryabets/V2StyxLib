package com.v2soft.styxlib.io;

import java.io.IOException;

public interface IStyxDataReader {
    short readUInt8() throws IOException;
    int readUInt16() throws IOException;
    long readUInt32() throws IOException;
    // For java this is 64 bit signed, but it should be interpreted as 64 bit unsigned.
    long readUInt64() throws IOException;
    long getUInt32();
    String readUTFString() throws IOException;
    int read(byte[] data, int offset, int dataLength) throws IOException;
}
