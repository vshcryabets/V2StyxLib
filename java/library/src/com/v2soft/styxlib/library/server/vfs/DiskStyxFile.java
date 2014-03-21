package com.v2soft.styxlib.library.server.vfs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

/**
 * Disk file representation
 * @author vshcryabets@gmail.com
 *
 */
public class DiskStyxFile extends MemoryStyxFile {
    protected File mFile;
    protected Map<ClientState, RandomAccessFile> mFilesMap;

    public DiskStyxFile(File file) throws IOException {
        super(file.getName());
        if ( !file.exists() ) {
            throw new IOException("File not exists");
        }
        mFile = file;
        mFilesMap = new HashMap<ClientState, RandomAccessFile>();
    }

    @Override
    public int getMode() {
        int rwx = (mFile.canRead() ? 1 << 2 : 0) |
                ( mFile.canWrite() ? 1 << 1 : 0) |
                ( mFile.canExecute() ? 1 : 0 );
        return rwx << 6 | rwx << 3 | rwx;
    }

    @Override
    public Date getAccessTime() {
        return new Date(mFile.lastModified());
    }

    @Override
    public Date getModificationTime() {
        return new Date(mFile.lastModified());
    }

    @Override
    public ULong getLength() {
        return new ULong(mFile.length());
    }

    @Override
    public String getOwnerName() {
        return "nobody";
    }

    @Override
    public String getGroupName() {
        return "nobody";
    }

    @Override
    public String getModificationUser() {
        return "nobody";
    }

    @Override
    public boolean open(ClientState client, int mode) throws IOException {
        boolean canOpen = false;
        String ramode = null;
        switch (mode) {
        case ModeType.OREAD:
            canOpen = mFile.canRead();
            ramode = "r";
            break;
        case ModeType.OWRITE:
            canOpen = mFile.canWrite();
            ramode = "rw";
            break;
        case ModeType.ORDWR:
            canOpen = mFile.canWrite() && mFile.canRead();
            ramode = "rw";
        default:
            break;
        }
        if ( canOpen ) {
            final RandomAccessFile rf = new RandomAccessFile(mFile, ramode);
            mFilesMap.put(client, rf);
        }
        return canOpen;
    }

    public int write(ClientState client, byte[] data, ULong offset) 
            throws StyxErrorMessageException {
        if ( mFilesMap.containsKey(client)) {
            final RandomAccessFile rf = mFilesMap.get(client);
            try {
                rf.seek(offset.asLong());
                rf.write(data);
            } catch (IOException e) {
                throw StyxErrorMessageException.newInstance(e.toString());
            }
        } else {
            throw StyxErrorMessageException.newInstance("File is not open");
        }
        return data.length;
    }

    @Override
    public long read(ClientState client, byte[] outbuffer, ULong offset, long count) 
            throws StyxErrorMessageException {
        if ( mFilesMap.containsKey(client)) {
            final RandomAccessFile rf = mFilesMap.get(client);
            try {
                rf.seek(offset.asLong());
                return rf.read(outbuffer, 0, (int) count);
            } catch (IOException e) {
                throw StyxErrorMessageException.newInstance(e.toString());
            }
        } else {
            throw StyxErrorMessageException.newInstance("File is not open");
        }
    }

    @Override
    public void close(ClientState client) {
        if ( mFilesMap.containsKey(client)) {
            RandomAccessFile rf = mFilesMap.get(client);
            mFilesMap.remove(rf);
            try {
                rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean delete(ClientState client) {
        super.delete(client);
        return mFile.delete();
    }

    @Override
    public void onConnectionClosed(ClientState state) {
        close(state);
    }
}