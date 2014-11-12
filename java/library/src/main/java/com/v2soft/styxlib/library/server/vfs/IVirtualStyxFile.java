package com.v2soft.styxlib.library.server.vfs;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Virtual styx file interface
 * @author vschryabets@gmail.com
 *
 */
public interface IVirtualStyxFile {
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
     * @param mode The mode argument specifies the access mode in which the file is to be opened.
     *  OREAD   = 0  // open read-only
     *  OWRITE  = 1  // open write-only
     *  ORDWR   = 2  // open read-write
     *  OEXEC   = 3  // execute (== read but check execute permission)
     *  OTRUNC  = 16 // or'ed in (except for exec), truncate file first
     *  OCEXEC  = 32 // or'ed in, close on exec
     *  ORCLOSE = 64 // or'ed in, remove on close
     *  Flags for the mode field in Topen and Tcreate messages
     * @throws IOException
     * @return true if file was successfully opened
     */
    public boolean open(ClientState client, int mode) throws IOException;
    /**
     * Close file
     * @param client
     */
    public void close(ClientState client);
    /**
     * Read from file
     * @param offset offset from begining of the file
     * @param count number of bytes to read
     * @return number of bytes that was readed into the buffer
     */
    public long read(ClientState client, byte[] buffer, ULong offset, long count) throws StyxErrorMessageException;
    public IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids)
            throws StyxErrorMessageException;
    /**
     * Writes data to file at the position offset.
     * @param client
     * @param data the data
     * @param offset offset from begining of the file
     * @return return the number of bytes written
     * @throws StyxErrorMessageException
     */
    public int write(ClientState client, byte[] data, ULong offset) throws StyxErrorMessageException;
    /**
     * Will be fired when client close connection to this server
     * @param state
     */
    public void onConnectionClosed(ClientState state);
    /**
     * Create new child file
     * @param name new file name
     * @param permissions
     * @param mode
     * @return QID of new file
     */
    public StyxQID create(String name, long permissions, int mode)
            throws StyxErrorMessageException;
    /**
     * Delete this file
     */
    public boolean delete(ClientState client);
}
