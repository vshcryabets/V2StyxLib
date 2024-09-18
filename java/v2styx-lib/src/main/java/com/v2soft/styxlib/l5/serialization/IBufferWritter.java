package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;

import java.io.IOException;

public interface IBufferWritter {
    void writeUInt8(short val) throws StyxException;
    void writeUInt16(int val) throws StyxException;
    void writeUInt32(long val) throws StyxException;
    void writeUInt64(long value) throws StyxException;
    void writeUTFString(String string) throws StyxException;
    int write(byte[] data, int offset, int count);
    // clean output and prepare to receieve data
    void prepareBuffer(int bufferSize);
}
