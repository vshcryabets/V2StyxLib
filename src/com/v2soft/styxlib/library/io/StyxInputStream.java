package com.v2soft.styxlib.library.io;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.types.ULong;

public class StyxInputStream extends FilterInputStream {

	public StyxInputStream(InputStream in) {
		super(in);
	}
	
	private long readInteger(int bytes) throws IOException
	{
		long result = 0L;
		// TODO optimize that method, it shuld use read(byte array)
		int shift = 0;
		for (int i=0; i<bytes; i++)
		{
			long b = (read()&0xFF);
//			if (b < 0)
//				throw new EOFException();
			if (shift > 0)
				b <<= shift;
			shift += 8;
			
			result |= b;
		}
		
		return result;
	}
	
	public final byte readByte() throws IOException
	{
		return (byte)readInteger(1);
	}
	
	public final short readShort() throws IOException
	{
		return (short)readInteger(2);
	}
	
	public final int readInt() throws IOException
	{
		return (int)readInteger(4);
	}
	
	public final long readLong() throws IOException
	{
		return readInteger(8);
	}
	
	public final short readUByte() throws IOException
	{
		return (short) (readByte() & 0xFF);
	}
	
	public final int readUShort() throws IOException
	{
		return readShort() & 0xFFFF;
	}
	
	public final long readUInt() throws IOException
	{
		return readInt() & 0xFFFFFFFFL;
	}
	
	public final ULong readULong() throws IOException
	{
		byte[] bytes = new byte[ULong.ULONG_LENGTH];
		read(bytes);
		
		return new ULong(bytes);
	}
	
	public final String readUTF() throws IOException
	{
		int count = readUShort();
		byte[] bytes = new byte[count];
		read(bytes);
		
		return utf8ToString(bytes);
	}
	
	public static String utf8ToString(byte[] bytes)
	{
		String result = null;
		try
		{
			result = new String(bytes, "UTF-8"); 
		} catch (UnsupportedEncodingException e)
		{ }
		
		return result;
	}

}
