package com.v2soft.styxlib.library.server.vfs;


public interface IVirtualStyxDirectory extends IVirtualStyxFile {
	public IVirtualStyxFile getFile(String path);
	public IVirtualStyxDirectory getDirectory(String path);
    public IVirtualStyxFile walk(String path);
}
