package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxNotAuthorizedException;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.serialization.IBufferWriter;
import com.v2soft.styxlib.l5.serialization.impl.BufferWriterImpl;
import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.io.File;
import java.util.*;

/**
 * Disk directory
 * @author vshcryabets@gmail.com
 *
 */
public class DiskStyxDirectory
extends DiskStyxFile {
    private Map<Integer, IBufferWriter> mBuffersMap;
    protected List<IVirtualStyxFile> mVirtualFiles;
    protected List<IVirtualStyxFile> mRealFiles;

    public DiskStyxDirectory(File directory, StyxSessionDI di) throws StyxException {
        super(directory, di);
        mQID = new QID(QidType.QTDIR, 0, mName.hashCode());
        mVirtualFiles = new ArrayList<>();
        mRealFiles = new ArrayList<>();
        mBuffersMap = new HashMap<>();
    }

    @Override
    public int getMode() {
        return (int) FileMode.Directory;
    }

    @Override
    public IVirtualStyxFile walk(int clienId, Queue<String> pathElements, List<QID> qids)
            throws StyxException {
        if ( !pathElements.isEmpty() ) {
            String filename = pathElements.poll();
            for (IVirtualStyxFile file : mVirtualFiles) {
                if ( file.getName().equals(filename)) {
                    qids.add(file.getQID());
                    return file.walk(clienId, pathElements, qids);
                }
            }
            // look at disk
            final File[] files = mFile.listFiles();
            for (File file : files) {
                if ( file.getName().equals(filename)) {
                    DiskStyxFile styxFile;
                    try {
                        if ( file.isDirectory() ) {
                            styxFile = new DiskStyxDirectory(file, mDI);
                        } else {
                            styxFile = new DiskStyxFile(file, mDI);
                        }
                        qids.add(styxFile.getQID());
                        return styxFile.walk(clienId, pathElements, qids);
                    } catch (StyxException e) {
                        throw StyxErrorMessageException.newInstance(e.toString());
                    }
                }
            }
            return null;
        }
        return super.walk(clienId, pathElements, qids);
    }

    @Override
    public boolean open(int clientId, int mode) throws StyxException {
        if (!mDI.getIsClientAuthorizedUseCase().isClientAuthorized(clientId)) {
            throw new StyxNotAuthorizedException();
        }
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result && mFile.canRead() ) {
            // load files
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mVirtualFiles) {
                final StyxStat stat = file.getStat();
                size += mDI.getDataSerializer().getStatSerializedSize(stat);
                stats.add(stat);
            }
            // reload disk files
            mRealFiles.clear();
            for (File file : mFile.listFiles()) {
                final var item = file.isDirectory() ? new DiskStyxDirectory(file, mDI)
                        : new DiskStyxFile(file, mDI);
                mRealFiles.add(item);
                final StyxStat stat = item.getStat();
                size += mDI.getDataSerializer().getStatSerializedSize(stat);
                stats.add(stat);
            }

            // allocate buffer
            var buffer = new BufferWriterImpl(size);
            for (StyxStat state : stats) {
                mDI.getDataSerializer().serializeStat(state, buffer);
            }
            mBuffersMap.put(clientId, buffer);
            return true;
        }
        return false;
    }

    @Override
    public int read(int clientId, byte[] outbuffer, long offset, int count) throws StyxErrorMessageException {
        if ( !mBuffersMap.containsKey(clientId)) {
            throw StyxErrorMessageException.newInstance("This file isn't open");
        }
        var buffer = mBuffersMap.get(clientId).getBuffer();
        int boffset = buffer.limit();
        if ( offset > boffset ) return 0;
        buffer.position((int) offset);
        int bleft = buffer.remaining();
        if ( count > bleft ) {
            count = bleft;
        }
        buffer.get(outbuffer, 0, count);
        return count;
    }

    @Override
    public void close(int clientId) {
        for (IVirtualStyxFile file : mVirtualFiles) {
            file.close(clientId);
        }
        mBuffersMap.remove(clientId);
    }

    /**
     * Add child file
     * @param file
     */
    public void addFile(IVirtualStyxFile file) {
        // TODO check! may be this folder already contains file with same name
        mVirtualFiles.add(file);
    }

    @Override
    public int write(int clientId, byte[] data, long offset)
            throws StyxErrorMessageException {
        throw StyxErrorMessageException.newInstance("Can't write to directory");
    }

    @Override
    public QID create(int clientId, String name, long permissions, int mode)
            throws StyxErrorMessageException {
        File newFile = new File(mFile, name);
        if ( newFile.exists() ) {
            throw StyxErrorMessageException.newInstance("Can't create file, already exists");
        }
        try {
            if ( (permissions & FileMode.Directory) != 0 ) {
                // create directory
                if ( !newFile.mkdir() ) {
                    throw StyxErrorMessageException.newInstance("Can't create directory, unknown error.");
                }
                DiskStyxDirectory file = new DiskStyxDirectory(newFile, mDI);
                return file.getQID();
            } else {
                // create file
                if ( !newFile.createNewFile() ) {
                    throw StyxErrorMessageException.newInstance("Can't create file, unknown error.");
                }
                DiskStyxFile file = new DiskStyxFile(newFile, mDI);
                return file.getQID();
            }
        } catch (Exception e) {
            throw StyxErrorMessageException.newInstance("Can't create file, unknown error. " + e.getMessage());
        }
    }
}
