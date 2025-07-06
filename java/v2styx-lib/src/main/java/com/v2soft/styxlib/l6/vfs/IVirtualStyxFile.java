package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

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
    StyxQID getQID();

    StyxStat getStat();
    /**
     * @return file access mode
     */
    int getMode();
    /**
     * @return file name
     */
    String getName();
    Date getAccessTime();
    Date getModificationTime();
    long getLength();
    String getOwnerName();
    String getGroupName();
    String getModificationUser();
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
     * @throws StyxException
     * @return true if file was successfully opened
     */
    boolean open(int clientId, int mode) throws StyxException;
    /**
     * Close file
     */
    void close(int clientId);
    /**
     * Read from file
     * @param offset offset from begining of the file
     * @param count number of bytes to read
     * @return number of bytes that was readed into the buffer
     */
    int read(int clientId, byte[] buffer, long offset, int count) throws StyxErrorMessageException;
    IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids)
            throws StyxErrorMessageException;
    /**
     * Writes data to file at the position offset.
     * @param data the data
     * @param offset offset from beginning of the file
     * @return return the number of bytes written
     * @throws StyxErrorMessageException
     */
    int write(int clientId, byte[] data, long offset) throws StyxErrorMessageException;

    /**
     * Will be fired when client connect to this server
     * @param client client information
     */
    void onConnectionOpened(int clientId);

    /**
     * Create new child file
     * @param name new file name
     * @param permissions file access permissions
     * @param mode create mode
     * @return QID of new file
     */
    StyxQID create(String name, long permissions, int mode)
            throws StyxErrorMessageException;
    /**
     * Delete this file
     */
    boolean delete(int clientId);

    /**
     * Release all resources.
     */
    void release() throws IOException;
}
