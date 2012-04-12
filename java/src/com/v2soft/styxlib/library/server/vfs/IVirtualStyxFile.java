package com.v2soft.styxlib.library.server.vfs;

import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;

public abstract interface IVirtualStyxFile {
	/**
	 * @return unical ID of the file
	 */
	public StyxQID getQID();

	public StyxStat getStat();
}
