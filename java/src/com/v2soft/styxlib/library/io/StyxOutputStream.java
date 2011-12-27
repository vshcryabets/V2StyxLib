package com.v2soft.styxlib.library.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.v2soft.styxlib.library.Consts;
import com.v2soft.styxlib.library.types.ULong;

public class StyxOutputStream extends FilterOutputStream {

	public StyxOutputStream(OutputStream out) {
		super(out);
	}
	
	private final void writeInteger(long value, int bytes) throws IOException
	{
		int shift = 0;
		// TODO optimize it
		for (int i=0; i<bytes; i++)
		{
			long __value = value;
			if (shift > 0)
				__value >>= shift;
			shift += 8;
			
			write((int)(__value & 0xFF));
		}
	}
	
	public final void writeByte(int value) throws IOException {
	    write(value);
	}
	
	public final void writeShort(int value) throws IOException
	{
		writeInteger(value, 2);
	}
	
	public final void writeInt(int value) throws IOException
	{
		writeInteger(value, 4);
	}
	
	public final void writeLong(long value) throws IOException
	{
		writeInteger(value, 8);
	}
	
	public final void writeUByte(int b) throws IOException
	{
		if (b < 0 || b > Consts.MAXUBYTE)
			throw new IllegalArgumentException(String.format("Value (%d) out of range of UByte (0-%d)",
					b, Consts.MAXUBYTE));
		writeByte(b);
	}
	
	public final void writeUShort(int s) throws IOException
	{
		if (s < 0 || s > Consts.MAXUSHORT)
			throw new IllegalArgumentException(String.format("Value (%d) out of range of UShort (0-%d)",
					Consts.MAXUSHORT));
		
		writeShort(s);
	}
	
	public final void writeUInt(long i) throws IOException
	{
		if (i < 0 || i > Consts.MAXUNINT)
			throw new IllegalArgumentException(String.format("Value (%d) out of range of UByte (0-%d)",
					i, Consts.MAXUNINT));
		
		writeInt((int)i);
	}
	
	public final void writeULong(ULong ul) throws IOException
	{
		byte[] bytes = ul.getBytes();
		write(bytes);
	}
	
	public final void writeUTF(String string) throws IOException
	{
		byte[] bytes = string.getBytes("UTF-8");
		writeUShort(bytes.length);
		if ( bytes.length > 0 )
			write(bytes);
	}
	
	public final void writeString(String string) throws IOException
	{
	    // TODO this is weird method
		byte[] bytes = string.getBytes("UTF-8");
		if (bytes.length > 0)
			write(bytes);
	}
}
