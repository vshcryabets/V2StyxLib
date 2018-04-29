package com.v2soft.styxlib.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.messages.base.enums.ModeType;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Disk file representation
 * @author vshcryabets@gmail.com
 *
 */
public class DiskStyxFile extends MemoryStyxFile {
    protected File mFile;
    protected Map<ClientDetails, RandomAccessFile> mFilesMap;

    public DiskStyxFile(File file) throws IOException {
        super(file.getName());
        if ( !file.exists() ) {
            throw new IOException("File not exists");
        }
        mFile = file;
        mFilesMap = new HashMap<ClientDetails, RandomAccessFile>();
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
    public long getLength() {
        return mFile.length();
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
    public boolean open(ClientDetails clientDetails, int mode) throws IOException {
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
            mFilesMap.put(clientDetails, rf);
        }
        return canOpen;
    }

    @Override
    public int write(ClientDetails clientDetails, byte[] data, long offset)
            throws StyxErrorMessageException {
        if ( mFilesMap.containsKey(clientDetails)) {
            final RandomAccessFile rf = mFilesMap.get(clientDetails);
            try {
                rf.seek(offset);
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
    public long read(ClientDetails clientDetails, byte[] outbuffer, long offset, long count)
            throws StyxErrorMessageException {
        if ( mFilesMap.containsKey(clientDetails)) {
            final RandomAccessFile rf = mFilesMap.get(clientDetails);
            try {
                rf.seek(offset);
                return rf.read(outbuffer, 0, (int) count);
            } catch (IOException e) {
                throw StyxErrorMessageException.newInstance(e.toString());
            }
        } else {
            throw StyxErrorMessageException.newInstance("File is not open");
        }
    }

    @Override
    public void close(ClientDetails clientDetails) {
        if ( mFilesMap.containsKey(clientDetails)) {
            RandomAccessFile rf = mFilesMap.get(clientDetails);
            mFilesMap.remove(rf);
            try {
                rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean delete(ClientDetails clientDetails) {
        super.delete(clientDetails);
        return mFile.delete();
    }

    @Override
    public void onConnectionClosed(ClientDetails state) {
        close(state);
    }
}
