package com.v2soft.styxlib.library.server.vfs;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

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
     * @param mode
     * @throws IOException 
     */
    public boolean open(ClientState client, int mode) throws IOException;
    /**
     * Close file
     * @param mode
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
     * Write data to file
     * @param client
     * @param data
     * @param offset
     * @return
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
     * @param name
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
