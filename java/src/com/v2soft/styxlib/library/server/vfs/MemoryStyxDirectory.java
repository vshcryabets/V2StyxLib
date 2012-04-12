package com.v2soft.styxlib.library.server.vfs;

import java.util.LinkedList;

import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.types.ULong;

public class MemoryStyxDirectory 
	implements IVirtualStyxDirectory {
	private LinkedList<IVirtualStyxDirectory> subdirs;
	
	@Override
	public StyxQID getQID() {
		return new StyxQID(QIDType.QTDIR, 0, new ULong(1));
	}

	@Override
	public IVirtualStyxFile getFile(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualStyxDirectory getDirectory(String path) {
		if ( path.length() == 0 || path.equals("/")) return this;
		return null;
	}

	@Override
	public StyxStat getStat() {
//		StyxStat result = new StyxStat(type, 
//				dev, 
//				getQID(), 
//				mode, 
//				accessTime, 
//				modificationTime, 
//				length, 
//				name, 
//				userName, 
//				groupName, 
//				modificationUser);
		// TODO Auto-generated method stub
		return null;
	}

}
