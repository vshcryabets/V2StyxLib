package com.v2soft.styxlib.l5.structs;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.IBufferWritter;
import com.v2soft.styxlib.l5.enums.QIDType;

import java.io.IOException;

/**
 * Styx QID structure
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxQID {
	public static final int CONTENT_SIZE = 13;
	public static final StyxQID EMPTY = new StyxQID(QIDType.QTFILE, 0L, 0L);

	private final QIDType mType; //the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.
	private final long mVersion; // version number for given path
	private final long mPath; //the file server's unique identification for the file

	public StyxQID(QIDType type, long version, long path) {
		mType = type;
		mVersion = version;
		mPath = path;
	}

    public StyxQID(IBufferReader input) throws IOException {
        mType = QIDType.factory(input.readUInt8());
        mVersion = input.readUInt32();
        mPath = input.readUInt64();
    }
    // ==================================================
    // Getters
    // ==================================================
	public QIDType getType(){return mType;}
    public long getVersion(){return mVersion;}
    public long getPath(){return mPath;}

	public void writeBinaryTo(IBufferWritter output) throws StyxException {
        output.writeUInt8((short) getType().getByte());
        output.writeUInt32(getVersion());
        output.writeUInt64(getPath());
    }

	@Override
	public String toString() {
		return String.format("(Type: %s; Version: %d; Path: %d)",
				getType(), getVersion(), getPath());
	}

}
