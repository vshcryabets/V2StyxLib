package com.v2soft.styxlib.io;

import java.io.IOException;

public interface IStyxDataReader {

    /**
     * Read 8-bits unsigned value.
     * @return 8-bits unsigned value.
     * @throws IOException
     */
    short readUInt8() throws IOException;

    /**
     * Read 16-bits unsigned value.
     * @return 16-bits unsigned value.
     * @throws IOException
     */
    int readUInt16() throws IOException;

    /**
     * Read 32-bits unsigned value.
     * @return 32-bits unsigned value.
     * @throws IOException
     */
    long readUInt32() throws IOException;

    // For java this is 64 bit signed, but it should be interpreted as 64 bit unsigned.
    long readUInt64() throws IOException;
    long getUInt32();
    String readUTFString() throws IOException;
    int read(byte[] data, int offset, int dataLength) throws IOException;
}
