package com.v2soft.styxlib.library.server.vfs;

import java.util.List;

import com.v2soft.styxlib.library.messages.base.structs.StyxQID;


public interface IVirtualStyxDirectory extends IVirtualStyxFile {
	public IVirtualStyxFile getFile(String path);
	public IVirtualStyxDirectory getDirectory(String path);
}
