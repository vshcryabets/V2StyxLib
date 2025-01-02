package com.v2soft.styxlib.l5.structs;

import com.v2soft.styxlib.l5.enums.QidType;

/**
 * Styx QID structure
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public record StyxQID(
	int type,//the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.
	long version, // version number for given path
	long path //the file server's unique identification for the file
) {
	public static final StyxQID EMPTY = new StyxQID(QidType.QTFILE, 0L, 0L);
}
