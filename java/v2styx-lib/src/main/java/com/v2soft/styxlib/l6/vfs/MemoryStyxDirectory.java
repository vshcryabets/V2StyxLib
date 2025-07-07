package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxNotAuthorizedException;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.serialization.IBufferWriter;
import com.v2soft.styxlib.l5.serialization.impl.BufferWriterImpl;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * In-Memory directory
 * @author vshcryabets@gmail.com
 *
 */
public class MemoryStyxDirectory
extends MemoryStyxFile {
    private Map<Integer, IBufferWriter> mBuffersMap;
    private List<IVirtualStyxFile> mFiles;

    public MemoryStyxDirectory(String name, StyxSessionDI di) {
        super(name, di);
        mQID = new StyxQID(QidType.QTDIR, 0, mName.hashCode());
        mFiles = new LinkedList<IVirtualStyxFile>();
        mBuffersMap = new HashMap<>();
    }

    @Override
    public int getMode() {
        return (int) FileMode.Directory;
    }

    @Override
    public IVirtualStyxFile walk(int clientId, Queue<String> pathElements, List<StyxQID> qids)
            throws StyxException {
        if (!pathElements.isEmpty() ) {
            String filename = pathElements.poll();
            for (IVirtualStyxFile file : mFiles) {
                if ( file.getName().equals(filename)) {
                    qids.add(file.getQID());
                    return file.walk(clientId, pathElements, qids);
                }
            }
        }
        throw StyxErrorMessageException.newInstance("File not found: " + pathElements.peek());
    }

    @Override
    public boolean open(int clientId, int mode) throws StyxException {
        if (!mDI.getIsClientAuthorizedUseCase().isClientAuthorized(clientId)) {
            throw new StyxNotAuthorizedException();
        }
        boolean result = ((mode&0x0F) == ModeType.OREAD);
        if ( result ) {
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mFiles) {
                final StyxStat stat = file.getStat();
                size += mDI.getDataSerializer().getStatSerializedSize(stat);
                stats.add(stat);
            }
            // allocate buffer
            IBufferWriter writer = new BufferWriterImpl(size);
            for (StyxStat state : stats) {
                mDI.getDataSerializer().serializeStat(state, writer);
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
