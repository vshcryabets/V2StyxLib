package com.v2soft.styxlib.library.server.vfs;

/**
 * Virtual Styx directory inteface
 * @author mrco
 *
 */
public interface IVirtualStyxDirectory extends IVirtualStyxFile {
	public IVirtualStyxFile getFile(String path);
	public IVirtualStyxDirectory getDirectory(String path);
}
