package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.serialization.IBufferWritter;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.BufferWritterImpl;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;

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
    private final IDataSerializer mSerializer;
    private Map<Integer, IBufferWritter> mBuffersMap;
    private List<IVirtualStyxFile> mFiles;

    public MemoryStyxDirectory(String name, IDataSerializer serializer) {
        super(name);
        mQID = new StyxQID(QidType.QTDIR, 0, mName.hashCode());
        mSerializer = serializer;
        mFiles = new LinkedList<IVirtualStyxFile>();
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
    public boolean open(int clientId, int mode) throws StyxException {
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result ) {
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mFiles) {
                final StyxStat stat = file.getStat();
                size += mSerializer.getStatSerializedSize(stat);
                stats.add(stat);
            }
            // allocate buffer
            IBufferWritter writer = new BufferWritterImpl(size);
            for (StyxStat state : stats) {
                mSerializer.serializeStat(state, writer);
            }
            mBuffersMap.put(clientId, writer);
        }
        return result;
    }

    @Override
    public int read(int clientId, byte[] outbuffer, long offset, int count) throws StyxErrorMessageException {
        if ( !mBuffersMap.containsKey(clientId))
            throw StyxErrorMessageException.newInstance("This file isn't open");
        final ByteBuffer buffer = mBuffersMap.get(clientId).getBuffer();
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
        for (IVirtualStyxFile file : mFiles) {
            file.close(clientId);
        }
        // remove buffer
        mBuffersMap.remove(clientId);
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
    public int write(int clientId, byte[] data, long offset)
            throws StyxErrorMessageException {
        throw StyxErrorMessageException.newInstance("Can't write to directory");
    }

    /**
     * Delete child file
     * @param file file that should be removed from this directory
     */
    public boolean deleteFile(IVirtualStyxFile file) {
        return mFiles.remove(file);
    }
}
