package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.serialization.IBufferWritter;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.BufferWritterImpl;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.File;
import java.util.*;

/**
 * Disk directory
 * @author vshcryabets@gmail.com
 *
 */
public class DiskStyxDirectory
extends DiskStyxFile {
    private Map<Integer, IBufferWritter> mBuffersMap;
    protected List<IVirtualStyxFile> mVirtualFiles;
    protected List<IVirtualStyxFile> mRealFiles;
    private IDataSerializer mSerializer;

    public DiskStyxDirectory(File directory, IDataSerializer serializer) throws StyxException {
        super(directory);
        mQID = new StyxQID(QidType.QTDIR, 0, mName.hashCode());
        mSerializer = serializer;
        mVirtualFiles = new ArrayList<>();
        mRealFiles = new ArrayList<>();
        mBuffersMap = new HashMap<>();
    }

    @Override
    public int getMode() {
        return (int) FileMode.Directory;
    }

    @Override
    public IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids)
            throws StyxErrorMessageException {
        if ( pathElements.hasNext() ) {
            String filename = pathElements.next();
            for (IVirtualStyxFile file : mVirtualFiles) {
                if ( file.getName().equals(filename)) {
                    qids.add(file.getQID());
                    return file.walk(pathElements, qids);
                }
            }
            // look at disk
            final File[] files = mFile.listFiles();
            for (File file : files) {
                if ( file.getName().equals(filename)) {
                    DiskStyxFile styxFile;
                    try {
                        if ( file.isDirectory() ) {
                            styxFile = new DiskStyxDirectory(file, mSerializer);
                        } else {
                            styxFile = new DiskStyxFile(file);
                        }
                        qids.add(styxFile.getQID());
                        return styxFile.walk(pathElements, qids);
                    } catch (StyxException e) {
                        throw StyxErrorMessageException.newInstance(e.toString());
                    }
                }
            }
            return null;
        }
        return super.walk(pathElements, qids);
    }

    @Override
    public boolean open(int clientId, int mode) throws StyxException {
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result && mFile.canRead() ) {
            // load files
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mVirtualFiles) {
                final StyxStat stat = file.getStat();
                size += mSerializer.getStatSerializedSize(stat);
                stats.add(stat);
            }
            // reload disk files
            mRealFiles.clear();
            for (File file : mFile.listFiles()) {
                final var item = file.isDirectory() ? new DiskStyxDirectory(file, mSerializer)
                        : new DiskStyxFile(file);
                mRealFiles.add(item);
                final StyxStat stat = item.getStat();
                size += mSerializer.getStatSerializedSize(stat);
                stats.add(stat);
            }

            // allocate buffer
            var buffer = new BufferWritterImpl(size);
            for (StyxStat state : stats) {
                mSerializer.serializeStat(state, buffer);
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
    public StyxQID create(String name, long permissions, int mode)
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
                DiskStyxDirectory file = new DiskStyxDirectory(newFile, mSerializer);
                return file.getQID();
            } else {
                // create file
                if ( !newFile.createNewFile() ) {
                    throw StyxErrorMessageException.newInstance("Can't create file, unknown error.");
                }
                DiskStyxFile file = new DiskStyxFile(newFile);
                return file.getQID();
            }
        } catch (Exception e) {
            throw StyxErrorMessageException.newInstance("Can't create file, unknown error. " + e.getMessage());
        }
    }
}
