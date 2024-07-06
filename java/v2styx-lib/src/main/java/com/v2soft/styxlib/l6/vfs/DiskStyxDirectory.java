package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.BufferWritterImpl;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QIDType;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Disk directory
 * @author vshcryabets@gmail.com
 *
 */
public class DiskStyxDirectory
extends DiskStyxFile {
    private Map<ClientDetails, ByteBuffer> mBuffersMap;
    protected List<IVirtualStyxFile> mVirtualFiles;
    protected List<IVirtualStyxFile> mRealFiles;
    private IDataSerializer mSerializer;

    public DiskStyxDirectory(File directory, IDataSerializer serializer) throws IOException {
        super(directory);
        mQID = new StyxQID(QIDType.QTDIR, 0, mName.hashCode());
        mSerializer = serializer;
        mVirtualFiles = new ArrayList<>();
        mRealFiles = new ArrayList<>();
        mBuffersMap = new HashMap<ClientDetails, ByteBuffer>();
    }

    @Override
    public int getMode() {
        return (int) (FileMode.Directory.getMode() | 0x01FF);
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
                    } catch (IOException e) {
                        throw StyxErrorMessageException.newInstance(e.toString());
                    }
                }
            }
            return null;
        }
        return super.walk(pathElements, qids);
    }

    @Override
    public boolean open(ClientDetails clientDetails, int mode) throws IOException {
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result && mFile.canRead() ) {
            // load files
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mVirtualFiles) {
                final StyxStat stat = file.getStat();
                size += stat.getSize();
                stats.add(stat);
            }
            // reload disk files
            mRealFiles.clear();
            for (File file : mFile.listFiles()) {
                final var item = file.isDirectory() ? new DiskStyxDirectory(file, mSerializer)
                        : new DiskStyxFile(file);
                mRealFiles.add(item);
                final StyxStat stat = item.getStat();
                size += stat.getSize();
                stats.add(stat);
            }

            // allocate buffer
            final ByteBuffer buffer = ByteBuffer.allocate(size);
            MetricsAndStats.byteBufferAllocation++;
            for (StyxStat state : stats) {
                mSerializer.serializeStat(state, new BufferWritterImpl(buffer));
            }
            mBuffersMap.put(clientDetails, buffer);
            return true;
        }
        return false;
    }

    @Override
    public long read(ClientDetails clientDetails, byte[] outbuffer, long offset, long count) throws StyxErrorMessageException {
        if ( !mBuffersMap.containsKey(clientDetails)) {
            throw StyxErrorMessageException.newInstance("This file isn't open");
        }
        final ByteBuffer buffer = mBuffersMap.get(clientDetails);
        int boffset = buffer.limit();
        if ( offset > boffset ) return 0;
        buffer.position((int) offset);
        int bleft = buffer.remaining();
        if ( count > bleft ) {
            count = bleft;
        }
        buffer.get(outbuffer, 0, (int) count);
        return count;
    }

    @Override
    public void close(ClientDetails clientDetails) {
        //        if ( !mBuffersMap.containsKey(client)) {
        //            throw StyxErrorMessageException.newInstance("This file isn't open");
        //        }
        // remove buffer
        mBuffersMap.remove(clientDetails);
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
    public int write(ClientDetails clientDetails, byte[] data, long offset)
            throws StyxErrorMessageException {
        throw StyxErrorMessageException.newInstance("Can't write to directory");
    }

    @Override
    public void onConnectionClosed(ClientDetails state) {
        for (IVirtualStyxFile file : mVirtualFiles) {
            file.onConnectionClosed(state);
        }
        mBuffersMap.remove(state);
    }

    @Override
    public StyxQID create(String name, long permissions, int mode)
            throws StyxErrorMessageException {
        File newFile = new File(mFile, name);
        if ( newFile.exists() ) {
            throw StyxErrorMessageException.newInstance("Can't create file, already exists");
        }
        try {
            if ( (permissions & FileMode.Directory.getMode()) != 0 ) {
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
        } catch (IOException e) {
            throw StyxErrorMessageException.newInstance("Can't create file, unknown error.");
        }
    }
}
