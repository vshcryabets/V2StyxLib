package com.v2soft.styxlib.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.messages.base.structs.StyxQID;
import com.v2soft.styxlib.messages.base.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;

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
     * @throws IOException
     * @return true if file was successfully opened
     */
    boolean open(ClientDetails clientDetails, int mode) throws IOException;
    /**
     * Close file
     */
    void close(ClientDetails clientDetails);
    /**
     * Read from file
     * @param inFileOffset offset from begining of the file
     * @param count number of bytes to read
     * @return number of bytes that was readed into the buffer
     */
    long read(ClientDetails clientDetails, byte[] buffer, long inFileOffset, long count) throws StyxErrorMessageException;
    IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids)
            throws StyxErrorMessageException;
    /**
     * Writes data to file at the position offset.
     * @param data the data
     * @param inFileOffset offset from begining of the file
     * @return return the number of bytes written
     * @throws StyxErrorMessageException in case of any error.
     */
    int write(ClientDetails clientDetails, byte[] data, long inFileOffset) throws StyxErrorMessageException;

    /**
     * Will be fired when client connect to this server
     * @param client client information
     */
    void onConnectionOpened(ClientDetails client);

    /**
     * Will be fired when client close connection to this server
     * @param client client information
     */
    void onConnectionClosed(ClientDetails client);

    /**
     * Create new child file
     * @param name new file name
     * @param permissions file access permissions
     * @param mode createFile mode
     * @return QID of new file
     */
    StyxQID createFile(String name, long permissions, int mode)
            throws StyxErrorMessageException;
    /**
     * Delete this file
     */
    void deleteFile(ClientDetails clientDetails) throws StyxErrorMessageException;

    /**
     * Release all resources.
     */
    void release() throws IOException;

}
