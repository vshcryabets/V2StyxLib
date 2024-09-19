package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;

import java.io.IOException;

public interface IBufferReader {
    short readUInt8() throws StyxException;
    int readUInt16() throws StyxException;
    long readUInt32() throws StyxException;
    // For java this is 64 bit signed, but it should be interpreted as 64 bit unsigned.
    long readUInt64() throws StyxException;
    long getUInt32();
    String readUTFString() throws StyxException;
    int readData(byte[] data, int offset, int dataLength) throws StyxException;
}
