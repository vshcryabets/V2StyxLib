package com.v2soft.styxlib.library.server.vfs;

import java.io.IOException;
import java.util.Date;

import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

/**
 * Virtual styx file interface
 * @author vschryabets@gmail.com
 *
 */
public abstract interface IVirtualStyxFile {
	/**
	 * @return unic ID of the file
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
	/**
	 * Open file
	 * @param mode
	 * @throws IOException 
	 */
    public boolean open(ClientState client, ModeType mode) throws IOException;
    /**
     * Close file
     * @param mode
     */
    public void close(ClientState client);
    /**
     * Read from file
     * @param offset offset from begining of the file
     * @param count number of bytes to read
     * @return
     */
    public byte[] read(ClientState client, ULong offset, long count);
}
