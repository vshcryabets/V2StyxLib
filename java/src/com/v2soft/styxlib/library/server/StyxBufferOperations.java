package com.v2soft.styxlib.library.server;

import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.types.ULong;

public abstract class StyxBufferOperations {
	protected abstract long getInteger(int bytes);
    protected abstract long readInteger(int bytes);
    protected abstract void writeInteger(int bytes, long value);
    public abstract String readUTF() throws UnsupportedEncodingException;
    public abstract ULong readUInt64();
	public abstract void clear();
	public abstract void limit(int value);
	public abstract void writeUTF(String protocolVersion) throws UnsupportedEncodingException;


    public long readUInt32() {
        return (readInteger(4) &0xFFFFFFFF);
    }

    public int readUInt16() {
        return (int) (readInteger(2)&0xFFFF);
    }

    public short readUInt8() {
        return (short) (readInteger(1)&0XFF);
    }

    public long getUInt32() {
        return getInteger(4) & 0xFFFFFFFFL;
    }
	public void writeUInt(long val) {
		writeInteger(4, val);
	}
	public void writeUByte(short val) {
		writeInteger(1, val);
	}
	public void writeUShort(int val) {
		writeInteger(2, val);
	}
}
