package com.v2soft.styxlib.library.messages.base.structs;

import java.io.IOException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;
import com.v2soft.styxlib.library.types.ULong;

public class StyxQID {
	public static final int CONTENT_SIZE = 13;
	public static final StyxQID EMPTY = new StyxQID(QIDType.QTFILE, 0L, ULong.ZERO);
	
	private QIDType mType; //the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word. 
	private long mVersion; // version number for given path 
	private ULong mPath; //the file server's unique identification for the file 
	
	public StyxQID(QIDType type, long version, ULong path)
	{
		mType = type;
		mVersion = version;
		mPath = path;
	}

    public StyxQID(StyxBufferOperations input) throws IOException {
        mType = QIDType.factory(input.readUInt8());
        mVersion = input.readUInt32();
        mPath = input.readUInt64();
    }
    public StyxQID(StyxInputStream input) throws IOException {
        mType = QIDType.factory(input.readUInt8());
        mVersion = input.readUInt32();
        mPath = input.readUInt64();
    }
    
	public QIDType getType(){return mType;}
    public long getVersion(){return mVersion;}
    public ULong getPath(){return mPath;}
	
	public void setType(QIDType type){mType = type;}
	public void setVersion(long version){mVersion = version;}
	public void setPath(ULong path){mPath = path;}
	
	public void writeBinaryTo(StyxBufferOperations output) throws IOException {
        output.writeUByte((short) getType().getByte());
        output.writeUInt(getVersion());
        output.writeUInt64(getPath());
    }
	
	@Override
	public String toString() {
		return String.format("(Type: %s; Version: %d; Path: %s)", 
				getType(), getVersion(), getPath().toString());
	}
	
}
