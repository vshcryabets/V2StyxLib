package com.v2soft.styxlib.library.server.vfs;

import com.v2soft.styxlib.library.messages.base.enums.QIDType;


public abstract class VirtualStyxDirectory extends VirtualStyxFile {

	public VirtualStyxDirectory(String filename) {
		super(filename);
		mQID.setType(QIDType.QTDIR);
	}

}
