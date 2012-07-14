package com.v2soft.styxlib.library.server.vfs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
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

    public DiskStyxFile(File parent, String name) {
        super(name);
        mFilesMap = new HashMap<ClientState, RandomAccessFile>();
        mFile = new File(parent, name);
        mStat = new StyxStat((short)0, 
                1, 
                mQID,
                getMode(),
                getAccessTime(), 
                getModificationTime(), 
                getLength(), 
                name, 
                getOwnerName(), 
                getGroupName(), 
                getModificationUser());
    }

    public DiskStyxFile(File file) {
        this(file.getParentFile(), file.getName());
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
            canOpen = (mode == ModeType.OREAD) && mFile.canRead();
            ramode = "r";
            break;
        case ModeType.OWRITE:
            canOpen = (mode == ModeType.OWRITE) && mFile.canWrite();
            ramode = "w";
        case ModeType.ORDWR:
            canOpen = (mode == ModeType.ORDWR) && mFile.canWrite() && mFile.canRead();
            ramode = "rw";
        default:
            break;
        }
        final RandomAccessFile rf = new RandomAccessFile(mFile, ramode);
        mFilesMap.put(client, rf);
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
    public void onConnectionClosed(ClientState state) {
        close(state);
    }

    private File getFile() {
        return mFile;
    }
}
