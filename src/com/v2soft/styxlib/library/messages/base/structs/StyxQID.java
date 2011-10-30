package com.v2soft.styxlib.library.messages.base.structs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.types.ULong;

public class StyxQID {
	public static final int CONTENT_SIZE = 13;
	public static final StyxQID EMPTY = new StyxQID(QIDType.QTFILE, 0L, ULong.ZERO);
	
	private QIDType mType;
	private long mVersion;
	private ULong mPath;
	
	public StyxQID(QIDType type, long version, ULong path)
	{
		mType = type;
		mVersion = version;
		mPath = path;
	}
	
	public StyxQID(InputStream stream) 
		throws IOException {
	    this(new StyxInputStream(stream));
	}
	
	public StyxQID(StyxInputStream input) throws IOException
	{
        mType = QIDType.factory(input.readUByte());
        mVersion = input.readUInt();
        mPath = input.readULong();
	}
	
	public QIDType getType()
	{
		return mType;
	}
	
	public void setType(QIDType type)
	{
		mType = type;
	}
	
	public long getVersion()
	{
		return mVersion;
	}
	
	public void setVersion(long version)
	{
		mVersion = version;
	}
	
	public ULong getPath()
	{
		return mPath;
	}
	
	public void setPath(ULong path)
	{
		mPath = path;
	}
	
	public byte[] getBinary()
		throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writeBinaryTo(stream);
				
		return stream.toByteArray();
	}
	
	public void writeBinaryTo(OutputStream stream) 
		throws IOException
	{
		StyxOutputStream output = new StyxOutputStream(stream);
		writeBinaryToOutput(output);
		output.flush();
	}
	
	public void writeBinaryTo(StyxOutputStream output) throws IOException
	{
		writeBinaryToOutput(output);
	}
	
	private void writeBinaryToOutput(StyxOutputStream output) throws IOException
	{
		output.writeUByte(getType().getByte());
		output.writeUInt(getVersion());
		output.writeULong(getPath());
	}
	
	@Override
	public String toString() {
		return String.format("(Type: %s; Version: %d; Path: %s)", 
				getType(), getVersion(), getPath().toString());
	}
	
}
