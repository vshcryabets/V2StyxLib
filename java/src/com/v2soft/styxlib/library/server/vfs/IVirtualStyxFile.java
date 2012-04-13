package com.v2soft.styxlib.library.server.vfs;

import java.util.Date;

import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.types.ULong;

public abstract interface IVirtualStyxFile {
	/**
	 * @return unical ID of the file
	 */
	public StyxQID getQID();

	public StyxStat getStat();
	/**
	 * @return file access mode
	 */
	public int getMode();
	/**
	 * @return file name
	 */
	public String getName();
	public Date getAccessTime();
	public Date getModificationTime();
	public ULong getLength();
	public String getOwnerName();
	public String getGroupName();
	public String getModificationUser();
}
