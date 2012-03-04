package com.v2soft.styxlib.library.server.vfs;

import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.types.ULong;

public abstract class VirtualStyxFile {
	protected StyxQID mQID;
	protected String mName;
	
	public VirtualStyxFile(String filename) {
		mName = filename;
		mQID = new StyxQID(QIDType.QTFILE, 0, ULong.ZERO);
	}
	
	public StyxQID getQID() {
		return mQID;
	}
}
