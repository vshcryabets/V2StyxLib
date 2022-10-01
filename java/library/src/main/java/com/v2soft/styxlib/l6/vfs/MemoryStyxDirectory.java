package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.l5.io.IStyxDataWriter;
import com.v2soft.styxlib.l5.io.StyxDataWriter;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QIDType;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * In-Memory directory
 * @author vshcryabets@gmail.com
 *
 */
public class MemoryStyxDirectory
extends MemoryStyxFile {
    private Map<ClientDetails, ByteBuffer> mBuffersMap;
    private List<IVirtualStyxFile> mFiles;

    public MemoryStyxDirectory(String name) {
        super(name);
        mQID.setType(QIDType.QTDIR);
        mFiles = new LinkedList<IVirtualStyxFile>();
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
    public boolean open(ClientDetails clientDetails, int mode) throws IOException {
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result ) {
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mFiles) {
                final StyxStat stat = file.getStat();
                size += stat.getSize();
                stats.add(stat);
            }
            // allocate buffer
            ByteBuffer buffer = ByteBuffer.allocate(size);
            MetricsAndStats.byteBufferAllocation++;
            IStyxDataWriter writer = new StyxDataWriter(buffer);
            for (StyxStat state : stats) {
                state.writeBinaryTo(writer);
            }
            mBuffersMap.put(clientDetails, buffer);
        }
        return result;
    }

    @Override
    public long read(ClientDetails clientDetails, byte[] outbuffer, long offset, long count) throws StyxErrorMessageException {
        if ( !mBuffersMap.containsKey(clientDetails)) StyxErrorMessageException.doException("This file isn't open");
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
        // remove buffer
        mBuffersMap.remove(clientDetails);
    }

    /**
     * Add child file
     * @param file
     */
    public IVirtualStyxFile addFile(IVirtualStyxFile file) {
//        String fileName = file.getName();
        // TODO check! may be this folder already contains file with same name
        mFiles.add(file);
        return this;
    }

    @Override
    public int write(ClientDetails clientDetails, byte[] data, long offset)
            throws StyxErrorMessageException {
        StyxErrorMessageException.doException("Can't write to directory");
        return 0;
    }

    @Override
    public void onConnectionClosed(ClientDetails state) {
        for (IVirtualStyxFile file : mFiles) {
            file.onConnectionClosed(state);
        }
        close(state);
    }

    /**
     * Delete child file
     * @param file file that should be removed from this directory
     */
    public boolean deleteFile(IVirtualStyxFile file) {
        return mFiles.remove(file);
    }
}
