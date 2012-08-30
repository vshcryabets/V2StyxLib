package com.v2soft.styxlib.library.server.vfs;

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

import org.apache.mina.core.buffer.IoBuffer;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.io.StyxByteBufferWriteable;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

/**
 * Disk directory
 * @author vshcryabets@gmail.com
 *
 */
public class DiskStyxDirectory
extends DiskStyxFile {
    private Map<ClientState, StyxByteBufferWriteable> mBuffersMap;
    protected Vector<IVirtualStyxFile> mFiles;
    protected List<IVirtualStyxFile> mDirectoryFiles;

    public DiskStyxDirectory(File directory) throws IOException {
        super(directory);
        mQID.setType(QIDType.QTDIR);
        mFiles = new Vector<IVirtualStyxFile>();
        mDirectoryFiles = new ArrayList<IVirtualStyxFile>();
        mBuffersMap = new HashMap<ClientState, StyxByteBufferWriteable>();
    }

    @Override
    public int getMode() {
        return (int) (FileMode.Directory.getMode() | 0x01FF);
    }

    @Override
    public IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids) {
        if ( pathElements.hasNext() ) {
            String filename = pathElements.next();
            for (IVirtualStyxFile file : mFiles) {
                if ( file.getName().equals(filename)) {
                    qids.add(file.getQID());
                    return file.walk(pathElements, qids);
                }
            }
            return null;
        }
        return super.walk(pathElements, qids);
    }

    @Override
    public boolean open(ClientState client, int mode) throws IOException {
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result && mFile.canRead() ) {
            // load files
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mFiles) {
                final StyxStat stat = file.getStat();
                size += stat.getSize();
                stats.add(stat);
            }
            // reload disk files
            mDirectoryFiles.clear();
            for (File file : mFile.listFiles()) {
                final DiskStyxFile item = new DiskStyxFile(file);
                mDirectoryFiles.add(item);
                final StyxStat stat = item.getStat();
                size += stat.getSize();
                stats.add(stat);
            }
            
            // allocate buffer
            final StyxByteBufferWriteable buffer = new StyxByteBufferWriteable(size);
            for (StyxStat state : stats) {
                state.writeBinaryTo(buffer);
            }
            mBuffersMap.put(client, buffer);
            return true;
        }
        return false;
    }

    @Override
    public long read(ClientState client, byte[] outbuffer, ULong offset, long count) throws StyxErrorMessageException {
        if ( !mBuffersMap.containsKey(client)) {
            throw StyxErrorMessageException.newInstance("This file isn't open");
        }
        final IoBuffer buffer = mBuffersMap.get(client).getBuffer();
        int boffset = buffer.limit();
        if ( offset.asLong() > boffset ) return 0;
        buffer.position((int) offset.asLong());
        int bleft = buffer.remaining();
        if ( count > bleft ) {
            count = bleft;
        }
        buffer.get(outbuffer, 0, (int) count);
        return count;
    }

    @Override
    public void close(ClientState client) {
//        if ( !mBuffersMap.containsKey(client)) {
//            throw StyxErrorMessageException.newInstance("This file isn't open");
//        }
        // remove buffer
        mBuffersMap.remove(client);
    }

    /**
     * Add child file
     * @param file
     */
    public void addFile(IVirtualStyxFile file) {
        // TODO check! may be this folder already contains file with same name 
        mFiles.add(file);
    }

    @Override
    public int write(ClientState client, byte[] data, ULong offset)
            throws StyxErrorMessageException {
        throw StyxErrorMessageException.newInstance("Can't write to directory");
    }

    @Override
    public void onConnectionClosed(ClientState state) {
        for (IVirtualStyxFile file : mFiles) {
            file.onConnectionClosed(state);
        }
        mBuffersMap.remove(state);
    }

    /**
     * Delete child file
     * @param file file that should be removed from this directory
     */
    public boolean deleteFile(IVirtualStyxFile file) {
        // FIXME
        return false;
//        return mFiles.remove(file);
    }
    
    @Override
    public StyxQID create(String name, long permissions, int mode)
            throws StyxErrorMessageException {
        File newFile = new File(mFile, name);
        if ( newFile.exists() ) {
            StyxErrorMessageException.doException("Can't create file, already exists");
        }
        try {
            if ( !newFile.createNewFile() ) {
                StyxErrorMessageException.doException("Can't create file, unknown error.");
            }
            DiskStyxFile file = new DiskStyxFile(newFile);
            return file.getQID();
        } catch (IOException e) {
            StyxErrorMessageException.doException("Can't create file, unknown error.");
        }
        return null;
    }
}
