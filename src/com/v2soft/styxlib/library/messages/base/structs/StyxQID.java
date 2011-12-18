package com.v2soft.styxlib.library.messages.base.structs;

import java.io.IOException;

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
	
	public StyxQID(StyxInputStream input) throws IOException {
        mType = QIDType.factory(input.readUByte());
        mVersion = input.readUInt();
        mPath = input.readULong();
	}
	
	public QIDType getType(){return mType;}
    public long getVersion(){return mVersion;}
    public ULong getPath(){return mPath;}
	
	public void setType(QIDType type){mType = type;}
	public void setVersion(long version){mVersion = version;}
	public void setPath(ULong path){mPath = path;}
	
	public void writeBinaryTo(StyxOutputStream output) throws IOException {
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
