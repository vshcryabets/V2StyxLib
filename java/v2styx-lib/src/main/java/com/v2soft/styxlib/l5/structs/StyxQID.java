package com.v2soft.styxlib.l5.structs;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.serialization.IBufferReader;

/**
 * Styx QID structure
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxQID {
	public static final StyxQID EMPTY = new StyxQID(QidType.QTFILE, 0L, 0L);

	private final int mType; //the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.
	private final long mVersion; // version number for given path
	private final long mPath; //the file server's unique identification for the file

	public StyxQID(int type, long version, long path) {
		mType = type;
		mVersion = version;
		mPath = path;
	}

	@Deprecated
    public StyxQID(IBufferReader input) throws StyxException {
        mType = input.readUInt8();
        mVersion = input.readUInt32();
        mPath = input.readUInt64();
    }
    // ==================================================
    // Getters
    // ==================================================
	public int getType(){return mType;}
    public long getVersion(){return mVersion;}
    public long getPath(){return mPath;}

	@Override
	public String toString() {
		return String.format("(Type: %s; Version: %d; Path: %d)",
				getType(), getVersion(), getPath());
	}

}
