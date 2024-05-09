package com.v2soft.styxlib.l5.serialization;

import java.io.IOException;

public interface IBufferWritter {
    void writeUInt8(short val) throws IOException;
    void writeUInt16(int val) throws IOException;
    void writeUInt32(long val) throws IOException;
    void writeUInt64(long value) throws IOException;
    void writeUTFString(String string) throws IOException;
    int write(byte[] data, int offset, int count);
    // clean output and prepare to receieve data
    void prepareBuffer(int bufferSize);
}
